package com.example.niolenelson.running.utilities

import android.app.Activity
import com.example.niolenelson.running.LocationSettingsUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap

class InteractiveDirectionsGenerator(activity: Activity, mMap: GoogleMap, val directions: List<LocalDirectionApi.Direction>) {
    private var requestingLocationUpdates = false

    private var locationRequest = LocationSettingsUtilities.getLocationRequest()

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult != null) {
                val lastLocation = locationResult.getLastLocation()
                val lat = lastLocation.latitude
                val lng = lastLocation.longitude
                println(lat)
                println(lng)
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
                    // TODO callback to the UI or send up a toast telling the user
                    println("location is not enabled")
                }
            }
        })
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        requestingLocationUpdates = false
    }

    fun startLocationUpdates() {
        requestingLocationUpdates = true
        println("start location updates")
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null)
    }

    fun isRequestingLocationUpdates(): Boolean {
        return requestingLocationUpdates
    }

}