package com.example.niolenelson.running

import android.app.Activity
import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.concurrent.Executor
import com.google.android.gms.location.LocationRequest



/**
 * Created by niolenelson on 9/23/18.
 */

object LocationSettingsUtilities {

    fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    private fun getLocationRequestTask(context: Activity, locationRequest: LocationRequest): Task<LocationSettingsResponse> {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        return client.checkLocationSettings(builder.build())
    }

    fun confirmLocationPermissions(context: Activity,  locationRequest: LocationRequest): Task<LocationSettingsResponse> {
        val task = getLocationRequestTask(context, locationRequest)
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                println(exception)
                exception.startResolutionForResult(context, exception.statusCode)
            } else {
                println("not resolvable $exception")
            }
        }

        return task
    }

}