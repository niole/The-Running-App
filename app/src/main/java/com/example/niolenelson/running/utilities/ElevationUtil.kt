package com.example.niolenelson.running.utilities

import android.graphics.Color
import com.google.maps.ElevationApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.ElevationResult
import com.google.maps.model.LatLng
import java.io.Serializable

data class Elevation(
        val start: LatLng,
        val end: LatLng,
        val startElevation: Double,
        val endElevation: Double
) : Serializable {
    fun diff(): Double {
        return endElevation - startElevation
    }

    fun color(): Int {
        val diff = diff()
        val index = Math.floor(Math.min(Math.abs(diff), 8.0) / 2.0).toInt()
        if (diff < 0) {
            return ElevationUtil.negColors[index]
        }
        return ElevationUtil.posColors[index]
    }
}

object ElevationUtil {
    private val zeroColor = Color.rgb(84, 13, 110)
    val negColors = listOf(
            zeroColor,
            Color.rgb(20, 13, 79),
            Color.rgb(78, 166, 153),
            Color.rgb(45, 216, 129),
            Color.rgb(111, 237, 183)
    )
    val posColors = listOf(
            zeroColor,
            Color.rgb(130, 2, 99),
            Color.rgb(217, 3, 104),
            Color.rgb(247, 92, 3),
            Color.rgb(247, 240, 82)
    )
    fun getElevationSeqments(context: GeoApiContext, directionsResult: DirectionsResult): List<Elevation> {
        val points = getElevationResult(context, directionsResult)
        return formatPoints(points)
    }

    private fun getElevationResult(context: GeoApiContext, directionsResult: DirectionsResult): Array<ElevationResult> {
        val routeLine = directionsResult.routes[0].overviewPolyline
        val elevationResult = ElevationApi.getByPoints(context, routeLine).await()
        return elevationResult
    }

    /**
     * how about we pick our windows and then color code
     * the line segments, easy peasy
     */
    private fun formatPoints(points: Array<ElevationResult>): List<Elevation> {
        val ps = points.toList()
        return ps.windowed(2, 1, false, {
            val first = it.first()
            val last = it.last()
    Elevation(
                start = first.location,
                end = last.location,
                startElevation = first.elevation,
                endElevation = last.elevation
            )
        })
    }
}
