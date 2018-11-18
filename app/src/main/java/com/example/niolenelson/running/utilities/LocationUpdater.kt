package com.example.niolenelson.running.utilities

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.example.niolenelson.running.LocationSettingsUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap

typealias LocationUpdateCallback = (lat: Double, lng: Double) -> Unit

class LocationUpdater(val activity: Activity, val mMap: GoogleMap) {
    private var requestingLocationUpdates = false

    private var locationUpdateCallback: LocationUpdateCallback = { _, _ -> }

    private var locationRequest = LocationSettingsUtilities.getLocationRequest()

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                val lastLocation = locationResult.getLastLocation()
                val lat = lastLocation.latitude
                val lng = lastLocation.longitude
                locationUpdateCallback(lat, lng)
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            println("onLocationAvailability: isLocationAvailable = ${locationAvailability.isLocationAvailable}")
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient = getFusedLocationProviderClient(activity)

    init {
        LocationPermissionHandler.getLocationPermission(activity, {
            mMap.setMyLocationEnabled(true)

            val task = LocationSettingsUtilities.confirmLocationPermissions(activity, locationRequest)

            task.addOnSuccessListener { locationSettingsResponse ->
                println(locationSettingsResponse.locationSettingsStates)
                val locationSettingsStates = locationSettingsResponse.locationSettingsStates
                val canTrackLocation = locationSettingsStates.isLocationUsable &&
                        locationSettingsStates.isLocationPresent &&
                        locationSettingsStates.isGpsPresent &&
                        locationSettingsStates.isGpsUsable
                if (canTrackLocation) {
                    println("location is enabled")
                    startLocationUpdates()
                } else {
                    println("location is not enabled")
                    Toast.makeText(activity, "You will not be able to use this app to its fullest potential without enabling gps and location.", Toast.LENGTH_LONG)
                }
            }
        })
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        requestingLocationUpdates = false
        println("stop location updates")
    }

    fun startLocationUpdates() {
        requestingLocationUpdates = true
        println("start location updates")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    fun onLocationUpdate(cb: LocationUpdateCallback) {
        locationUpdateCallback = cb
    }

    fun isRequestingLocationUpdates(): Boolean {
        return requestingLocationUpdates
    }
}