package com.example.niolenelson.running.utilities

import com.google.android.gms.maps.GoogleMap
import com.google.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.LatLng as GmsLatLng

/**
 * Created by niolenelson on 9/22/18.
 */

object RouteUtilities {
    private val COLOR_ORANGE_ARGB = -0xa80e9

    fun setPolylineFromElevationDetails(es: List<Elevation>, mMap: GoogleMap) {
        es.forEach {
            mMap.addPolyline(RouteUtilities.makePolyline(listOf(it.start, it.end), it.color()))
        }
    }

    fun makePolyline(points: List<LatLng>, color: Int): PolylineOptions {
        return PolylineOptions()
                .add(*points.map{ GmsLatLng(it.lat, it.lng) }.toTypedArray())
                .color(color)
    }

    fun makePolyline(points: List<LatLng>): PolylineOptions {
        return PolylineOptions()
                .add(*points.map{ GmsLatLng(it.lat, it.lng) }.toTypedArray())
                .color(COLOR_ORANGE_ARGB)
    }
}