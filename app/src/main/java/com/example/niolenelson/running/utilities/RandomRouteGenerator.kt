package com.example.niolenelson.running.utilities

import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.*

data class Route(
        val angle: Double,
        val points: List<LatLng>,
        val directionsSoFar: DirectionsResult?,
        val elevationDetails: List<Elevation>
) {
    fun getEncodedPolyline(): EncodedPolyline? {
        if (directionsSoFar != null) {
            return directionsSoFar.routes[0].overviewPolyline
        }
        return null
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

    private val stepDistances = getAllStepDistances()

    var routes: ArrayList<Route> = arrayListOf()

    fun next(): Route? {
        val newRoute = generateNewRoute()
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

    private fun generateNewRoute(): Route? {
        // get next point based on the angle currently at and then angles
        // possible to use
        return getNextPoint(turningRadii, listOf(start))
    }

    private fun getStepDistance(): Double {
        return stepDistances.shuffled().first()
    }

    /**
     * pick random angle per point, repeat no angle picks
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
                            getStepDistance(),
                            angle
                    )
            val waypoints = route.subList(1, route.size).plus(nextPoint)
            val request = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.WALKING)
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
                    val elevationResult = ElevationUtil.getElevationSeqments(geoApiContext, directionsResult)
                    return Route(angle, points, directionsResult, elevationResult)
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
            return possibleDistanceMiles <= (distanceMiles + .5) && possibleDistanceMiles >= distanceMiles
        }
        return false
    }

    private fun getAllStepDistances(): List<Double> {
        val midPoint = distanceMiles / 2
        val totalMultiplesOfQuarterMile = Math.max(1.0, Math.floor(midPoint / 0.25))
        return (1 until (totalMultiplesOfQuarterMile + 1.0).toInt()).map { it * 0.25 }
    }

}