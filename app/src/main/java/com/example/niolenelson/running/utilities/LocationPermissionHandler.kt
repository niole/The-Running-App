package com.example.niolenelson.running.utilities

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast

object LocationPermissionHandler {
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 1

    private var attemptEnablePermissions = false

    private var mLocationPermissionGranted = false

    fun getLocationPermission(activityContext: Activity, cb: () -> Unit) {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(activityContext.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            cb()
        } else {
            ActivityCompat.requestPermissions(activityContext,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            if (!attemptEnablePermissions) {
                attemptEnablePermissions = true
                getLocationPermission(activityContext, cb)
            } else {
                Toast.makeText(activityContext, "You must enable location access for this app to work properly", Toast.LENGTH_LONG)
            }
        }
    }
}
