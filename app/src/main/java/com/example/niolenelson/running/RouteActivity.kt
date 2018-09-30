package com.example.niolenelson.running

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.SupportMapFragment
import com.example.niolenelson.running.utilities.LocalDirectionApi
import com.example.niolenelson.running.utilities.RouteUtilities.makePolyline
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.example.niolenelson.running.utilities.Haversine.metersToMiles


/**
 * Created by niolenelson on 9/22/18.
 */

class RouteActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: Int = 1

    private var attemptEnablePermissions = false

    private var mLocationPermissionGranted = false

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private lateinit var startingPoint: LatLng

    private lateinit var directions: List<LocalDirectionApi.Direction>

    private lateinit var locationCallback: LocationCallback

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private var requestingLocationUpdates: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        startingPoint = intent.extras.get("startingPoint") as LatLng
        val directionsResult = intent.extras.get("directionsResult") as DirectionsResult
        directions = LocalDirectionApi.getDirections(directionsResult)

        println("ROUTE DISTANCE                   kjkkjk  kj")
        println(directions.fold(0.toDouble()) {
           distance, nextDirection -> distance + metersToMiles(nextDirection.distanceMeters.toDouble())
        })

        fusedLocationClient = getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
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
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        requestingLocationUpdates = false
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun startLocationUpdates() {
        requestingLocationUpdates = true
        println("start location updates")
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null)
    }

    private fun getLocationPermission(cb: (enabled: Boolean) -> Unit) {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */


        if (ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            cb(true)
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            cb(false)
            if (!attemptEnablePermissions) {
                attemptEnablePermissions = true
                getLocationPermission(cb)
            }
        }
    }

    private fun setupLocation() {
        locationRequest = LocationSettingsUtilities.getLocationRequest()
        getLocationPermission {
            isEnabled ->
            if (isEnabled ) {
                mMap.setMyLocationEnabled(true)

                val task = LocationSettingsUtilities.confirmLocationPermissions(this, locationRequest)

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
                    }
                }

            } else {
                println("not enbaled")
            }

        }
    }

    private fun drawRoute() {
        val newLinePoints = directions.flatMap { direction -> direction.polyline.decodePath() }
        val newLine = makePolyline(newLinePoints)
        mMap.addPolyline(newLine)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()
        mMap.moveCamera(CameraUpdateFactory.newLatLng(com.google.android.gms.maps.model.LatLng(startingPoint.lat, startingPoint.lng)))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.toFloat()))
        mMap.uiSettings.setZoomControlsEnabled(true)

        setupLocation()

        drawRoute()
    }

}

