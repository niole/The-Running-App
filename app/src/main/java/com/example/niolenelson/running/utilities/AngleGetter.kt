package com.example.niolenelson.running.utilities

import com.google.maps.model.LatLng

/**
 * Created by niolenelson on 7/22/18.
 */
object AngleGetter {

    private fun getVectorNorm(v: LatLng): Double {
        return Math.sqrt(Math.pow(v.lat, 2.0) + Math.pow(v.lng, 2.0))
    }

    fun getAngleBetweenVectors(p1: LatLng, p2: LatLng): Double {
        val p1Norm = getVectorNorm(p1)
        val p2Norm = getVectorNorm(p2)
        val dotProduct = (p1.lat * p2.lat) + (p1.lng * p2.lng)

        return Math.acos(dotProduct / (p1Norm * p2Norm))
    }

    fun getAngleFromNorthOnUnitCircle(point: LatLng): Double {
        val p1Norm = getVectorNorm(point)
        val p2Norm = 1

        val dotProduct = (0 * point.lat) + (point.lng * 1.0)

        return Math.acos(dotProduct / (p1Norm * p2Norm))

    }

}
