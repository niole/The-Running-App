package com.example.niolenelson.running.utilities

import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.model.LatLng

class MatrixRouteGenerator(val routeError: Double, val start: LatLng, val routeLengthMeters: Double, val geoApiContext: GeoApiContext, val wayPoints: List<LatLng>) {
    var edgeMap: MutableMap<LatLng, List<Pair<LatLng, Double>>> = mutableMapOf()

    var paths: List<List<LatLng>> = listOf()

    init {
        buildMap(wayPoints)
        setPaths()
    }

    private fun setPaths() {
    }

    private fun buildMap(points: List<LatLng>) {
        points.forEachIndexed { index, origin ->
            val dests = points.subList(index + 1, points.size).toTypedArray()
            val matrix = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(origin)
                    .destinations(*dests)
                    .await()
            matrix.rows.forEach {row ->
                row.elements.forEachIndexed { index, it ->
                    val distance = it.distance.inMeters
                    val destinationPoint = dests[index]
                    val foundEdges = edgeMap.get(origin)
                    if (foundEdges == null) {
                        edgeMap.plus(Pair(origin, listOf(Pair(destinationPoint, distance))))
                    } else {
                        edgeMap.plus(Pair(origin, foundEdges.plus(Pair(destinationPoint, distance))))
                    }

                    val foundDestinationPointsEdges = edgeMap.get(destinationPoint)
                    if (foundDestinationPointsEdges == null) {
                        edgeMap.plus(Pair(destinationPoint, listOf(Pair(origin, distance))))
                    } else {
                        edgeMap.plus(Pair(destinationPoint, foundDestinationPointsEdges.plus(Pair(origin, distance))))
                    }

                }
            }
        }
    }
}

