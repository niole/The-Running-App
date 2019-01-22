package com.example.niolenelson.running.utilities

typealias LocationUpdateCallback = (lat: Double, lng: Double) -> Unit

abstract class LocationUpdaterBase {
    var requestingLocationUpdates: Boolean = false

    var locationUpdateCallback: LocationUpdateCallback = { _, _ -> }

    fun onLocationUpdate(cb: LocationUpdateCallback) {
        locationUpdateCallback = cb
    }

    open fun startLocationUpdates() {
        requestingLocationUpdates = true
    }

    open fun stopLocationUpdates() {
        requestingLocationUpdates = false
    }

    fun isRequestingLocationUpdates(): Boolean {
        return requestingLocationUpdates
    }
}
