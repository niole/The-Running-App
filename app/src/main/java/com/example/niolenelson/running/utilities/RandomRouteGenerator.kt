package com.example.niolenelson.running.utilities

import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng

data class Route(
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

    private val totalRotation: Double = 360.0

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

    fun getTotalRoutes(): Int {
        return routes.size
    }

    fun getRouteAtIndex(index: Int): Route {
       if (index < routes.size) {
           return routes[index]
       }
       return routes.last()
    }

    private fun generateNewRoute(currentRoute: List<LatLng>): Route? {
        var routeSoFar = getNextPoint(currentRoute)
        while (routeSoFar != null && !isRouteComplete(routeSoFar.distance())) {
            routeSoFar = getNextPoint(routeSoFar.points)
        }
        return routeSoFar
    }

    private fun getNextPoint(currentRoute: List<LatLng>): Route? {
        val lastPoint = currentRoute.last()
        var attempts = 0
        var distance: Double? = null
        var directionsResult: DirectionsResult? = null
        var nextPoint: LatLng? = null

        while (!isAcceptableDistance(distance) && attempts < 11) {
            attempts += 1
            val nextBearing = Math.ceil(Math.random() * totalRotation)
            nextPoint = Haversine.getLocationXDistanceFromLocationKM(
                    lastPoint.lat,
                    lastPoint.lng,
                    stepDistance,
                    nextBearing
            )
            val request = DirectionsApi.newRequest(geoApiContext)
                    .origin(start)
                    .waypoints(
                            *currentRoute
                                    .subList(1, currentRoute.size)
                                    .plus(nextPoint).toTypedArray()
                    )
                    .destination(start)
            directionsResult = request.await()
            val legDistances = directionsResult.routes.map {
                it.legs.sumByDouble { leg -> leg.distance.inMeters.toDouble() }
            }
            distance = legDistances.find { isAcceptableDistance(it) }
        }

        if (distance != null && nextPoint != null) {
            return Route(currentRoute.plus(nextPoint), directionsResult)
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