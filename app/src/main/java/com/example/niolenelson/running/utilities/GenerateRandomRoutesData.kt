package com.example.niolenelson.running.utilities

import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.RoadsApi
import com.google.maps.model.Bounds
import com.google.maps.model.LatLng
import com.google.maps.model.SnappedPoint
import com.google.android.gms.maps.model.LatLng as GmsLatLng

class GenerateRandomRoutesData(val geoContext: GeoApiContext, val start: LatLng, val routeDistanceMiles: Double) {
    private lateinit var routeBounds: LatLngBounds

    var routesData: List<LatLng> = listOf()

    init {
       routesData = getGeneratedRoutesData()
    }

    fun getGeneratedRoutesData(): List<LatLng> {
        if (routesData.isNotEmpty()) {
           return routesData
        }
        return generateRoutesData()
    }

    private fun generateRoutesData(): List<LatLng> {
        val totalBoundFinds = Math.ceil(routeDistanceMiles / 2).toInt()

        val point = start
        val bounds = getBounds(point)
        val surroundingBounds = getSurroundingGridBounds(bounds.northeast, bounds.southwest)

        var res: List<LatLng> = listOf()

        if (totalBoundFinds >= 1) {
            res = res.plus(setPathDataInBounds(surroundingBounds))
        }

        for (i in 0..totalBoundFinds) {
            res = res.plus(setPathDataInBounds(
                    getSurroundingGridBounds(
                            LatLng(routeBounds.northeast.latitude, routeBounds.northeast.longitude),
                            LatLng(routeBounds.southwest.latitude, routeBounds.southwest.longitude)
                    )
            ))
        }
        return res
    }

    /**
     * gets points on roads in bounded patches described in the arguments
     */
    private fun setPathDataInBounds(surroundingBounds: Array<LatLngBounds>): List<LatLng> {
        val markerAddresses: List<SnappedPoint> = surroundingBounds.flatMap {
            getPointsInBounds(
                    LatLng(it.northeast.latitude, it.northeast.longitude),
                    LatLng(it.southwest.latitude, it.southwest.longitude)
            ).asIterable()
        }

        return markerAddresses.map { it.location }
    }

    private fun getDistanceBetweenCoordinates(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {
        return Haversine.distanceMiles(lat1, lon1, lat2, lon2)
    }

    /**
     * returns distance between coords in a path in miles
     */
    private fun getPathDistance(path: List<LatLng>): Double {
        var distance = 0.toDouble()
        if (path.size > 1) {
            for (i in 1..(path.size - 1)) {
                val curr = path[i]
                val prev = path[i - 1]
                distance += getDistanceBetweenCoordinates(
                        curr.lat,
                        prev.lat,
                        curr.lng,
                        prev.lng
                )
            }
        }

        return distance
    }

    /**
     * get surrounding 8 squares that surround original bounding area
     */
    private fun getSurroundingGridBounds(northeast: LatLng, southwest: LatLng): Array<LatLngBounds> {
        val squareHeight = Math.abs(northeast.lat - southwest.lat)
        val squareWidth = Math.abs(northeast.lng - southwest.lng)

        val topSquareSW = GmsLatLng(northeast.lat, southwest.lng)
        val topSquareNE = GmsLatLng(northeast.lat + squareHeight, northeast.lng)
        val topSquare = LatLngBounds(topSquareSW, topSquareNE)

        val topRightSquare = LatLngBounds(
                GmsLatLng(topSquareSW.latitude, topSquareSW.longitude + squareWidth),
                GmsLatLng(topSquareNE.latitude, topSquareSW.longitude + squareWidth)
        )

        val topLeftSquare = LatLngBounds(
                GmsLatLng(topSquareSW.latitude, topSquareSW.longitude - squareWidth),
                GmsLatLng(topSquareNE.latitude, topSquareSW.longitude - squareWidth)
        )

        val leftSquare = LatLngBounds(
                GmsLatLng(southwest.lat, southwest.lng - squareWidth),
                GmsLatLng(northeast.lat, northeast.lng - squareWidth)
        )

        val rightSquare = LatLngBounds(
                GmsLatLng(southwest.lat, southwest.lng + squareWidth),
                GmsLatLng(northeast.lat, northeast.lng + squareWidth)
        )

        val bottomMiddleSquare = LatLngBounds(
                GmsLatLng(southwest.lat - squareHeight, southwest.lng),
                GmsLatLng(northeast.lat - squareHeight, northeast.lng)
        )

        val bottomRightSquare = LatLngBounds(
                GmsLatLng(southwest.lat - squareHeight, southwest.lng + squareWidth),
                GmsLatLng(northeast.lat - squareHeight, northeast.lng + squareWidth)
        )

        val bottomLeftSquare = LatLngBounds(
                GmsLatLng(southwest.lat - squareHeight, southwest.lng - squareWidth),
                GmsLatLng(northeast.lat - squareHeight, northeast.lng - squareWidth)
        )

        routeBounds = LatLngBounds(
                bottomLeftSquare.southwest,
                topRightSquare.northeast
        )

        return arrayOf(
                topSquare,
                topRightSquare,
                topLeftSquare,
                leftSquare,
                rightSquare,
                bottomMiddleSquare,
                bottomRightSquare,
                bottomLeftSquare
        )
    }

    /**
     * get view points bounds as lat lng points
     */
    private fun getBounds(latLng: LatLng): Bounds {
        val address = GeocodingApi.reverseGeocode(geoContext, latLng).await()
        return address[0].geometry.viewport
    }

    private fun getPointsInBounds(northeast: LatLng, southwest: LatLng): Array<SnappedPoint> {
        val points = RoadsApi.nearestRoads(geoContext, northeast, southwest).await()
        return points ?: arrayOf()
    }

}
