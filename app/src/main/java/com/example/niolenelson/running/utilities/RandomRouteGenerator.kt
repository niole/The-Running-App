package com.example.niolenelson.running.utilities

import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.RoadsApi
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng

data class Route(
        val angle: Double,
        val points: List<LatLng>,
        val directionsSoFar: DirectionsResult?
) {
    fun distance(): Double {
        if (directionsSoFar != null) {
            val legDistances = directionsSoFar.routes.map {
                it.legs.sumByDouble { leg -> leg.distance.inMeters.toDouble() }
            }
            return legDistances[0]
        }
        return 0.0
    }
}

/**
 * generates random routes as we go
 * always returns a suitable route
 * caches directions for a route when it is generated
 */
class RandomRouteGenerator(
        val geoApiContext: GeoApiContext,
        val start: LatLng,
        val distanceMiles: Double) {

    private val turningRadii: List<Double> = listOf(0.0, 30.0, 60.0, 90.0, 120.0, 140.0, 170.0, 300.0, 330.0)

    private val stepDistance: Double = 0.5

    var routes: ArrayList<Route> = arrayListOf()

    fun next(): Route? {
        val newRoute = generateNewRoute(listOf(start))
        if (newRoute != null) {
            routes.add(newRoute)
            return newRoute
        }
        return null
    }

    fun getRouteAtIndex(index: Int): Route {
       if (index < routes.size) {
           return routes[index]
       }
       return routes.last()
    }

    private fun getRandomAngle(possibleAngles: List<Double>): Double {
        if (possibleAngles.isNotEmpty()) {
            return possibleAngles.shuffled().first()
        }
        return 0.0
    }

    private fun generateNewRoute(currentRoute: List<LatLng>): Route? {
        // get next point based on the angle currently at and then angles
        // possible to use
        return getNextPoint(turningRadii, listOf(start))
    }


    /**
     * depending on length of the route, only max turns of a certain size will matter
     * and because you go left in a circle or right in a circle, then depeding on the distance
     * and the step distance, you should turn more or less
     *
     * To test this, let's make the turn radius limit always the same
     * let's not
     */
    private fun getNextPoint(
            angleChoices: List<Double>,
            route: List<LatLng>
    ): Route? {
        var angles = angleChoices
        val lastPoint = route.last()

        while (angles.isNotEmpty()) {
            val angle = getRandomAngle(angles)
            angles = angles.filter { it != angle }
            val nextPoint = Haversine.getLocationXDistanceFromLocationKM(
                            lastPoint.lat,
                            lastPoint.lng,
                            stepDistance,
                            angle
                    )
            val waypoints = route.subList(1, route.size).plus(nextPoint)
            val request = DirectionsApi.newRequest(geoApiContext)
                    .origin(start)
                    .waypoints(*waypoints.toTypedArray())
                    .destination(start)
            val directionsResult = request.await()

            val legDistances = directionsResult.routes.map {
                it.legs.sumByDouble { leg -> leg.distance.inMeters.toDouble() }
            }
            val acceptableDistance = legDistances.find { isAcceptableDistance(it) }
            if (acceptableDistance != null) {
                val points = listOf(start).plus(waypoints)
                if (isRouteComplete(acceptableDistance)) {
                    println("COMPLETE ROUTE: points: $points, length: $acceptableDistance")
                    return Route(angle, points, directionsResult)
                } else {
                    val filteredAngles = turningRadii
                    val completedRoute: Route? = getNextPoint(filteredAngles, points)
                    if (completedRoute != null) {
                        return completedRoute
                    }
                    // if we get to here, then let it run out
                }
            }

        }
        return null
    }

    private fun isAcceptableDistance(distanceMeters: Double?): Boolean {
        if (distanceMeters != null) {
            val possibleDistanceMiles = Haversine.metersToMiles(distanceMeters)
            return possibleDistanceMiles <= distanceMiles + .5
        }
        return false
    }

    private fun isRouteComplete(distanceMeters: Double): Boolean {
        if (distanceMeters != null) {
            val possibleDistanceMiles = Haversine.metersToMiles(distanceMeters)
            return possibleDistanceMiles <= distanceMiles + .5 && possibleDistanceMiles >= distanceMiles - .5
        }
        return false
    }

}