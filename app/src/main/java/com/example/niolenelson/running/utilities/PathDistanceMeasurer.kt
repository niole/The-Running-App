package com.example.niolenelson.running.utilities

/**
 * Created by niolenelson on 7/22/18.
 */
object Haversine {
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double, unit: Char): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60.0 * 1.1515
        if (unit == 'K') {
            dist = dist * 1.609344
        } else if (unit == 'N') {
            dist = dist * 0.8684
        }
        return dist
    }

    fun distanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return distance(lat1, lon1, lat2, lon2, 'K') / 1.609
    }

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return distance(lat1, lon1, lat2, lon2, 'K') * 1000
    }

    fun milesToMeters(miles: Double): Double {
        return miles * 1609
    }

    fun metersToMiles(meters: Double): Double {
        return meters / 1609
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}
