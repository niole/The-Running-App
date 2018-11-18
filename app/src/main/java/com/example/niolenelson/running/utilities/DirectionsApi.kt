package com.example.niolenelson.running.utilities

import com.google.maps.model.DirectionsResult
import com.google.maps.model.EncodedPolyline
import com.google.maps.model.LatLng

/**
 * Created by niolenelson on 9/22/18.
 */

object LocalDirectionApi {

    data class Direction(
        val start: LatLng,
        val end: LatLng,
        val htmlInstructions: String,
        val polyline: EncodedPolyline,
        val durationSeconds: Long,
        val distanceMeters: Long,
        val distance: String,
        val duration: String
    )

    fun getDistance(directions: List<Direction>): Double {
        return directions.fold(0.toDouble()) {
            distance, nextDirection -> distance + Haversine.metersToMiles(nextDirection.distanceMeters.toDouble())
        }
    }

    fun getDirections(result: DirectionsResult): List<Direction> {
        return result.routes[0].legs.flatMap {
            leg -> leg.steps.map {
                step -> Direction (
                    start = step.startLocation,
                    end = step.endLocation,
                    htmlInstructions = step.htmlInstructions,
                    polyline = step.polyline,
                    durationSeconds = step.duration.inSeconds,
                    distanceMeters = step.distance.inMeters,
                    distance = step.distance.humanReadable,
                    duration = step.duration.humanReadable
                )
            }
        }
    }

}