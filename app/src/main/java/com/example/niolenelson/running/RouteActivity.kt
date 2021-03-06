package com.example.niolenelson.running

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.SupportMapFragment
import com.example.niolenelson.running.utilities.LocalDirectionApi
import com.example.niolenelson.running.utilities.RouteUtilities.makePolyline
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.LatLng
import com.example.niolenelson.running.utilities.InteractiveDirectionsGenerator

/**
 * Created by niolenelson on 9/22/18.
 */

class RouteActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private var interactiveDirectionsGenerator: InteractiveDirectionsGenerator? = null

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private lateinit var startingPoint: LatLng

    private lateinit var directions: List<LocalDirectionApi.Direction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        startingPoint = intent.extras.get("startingPoint") as LatLng
        val directionsResult = intent.extras.get("directionsResult") as DirectionsResult
        directions = LocalDirectionApi.getDirections(directionsResult)

        println("ROUTE DISTANCE")
        println(LocalDirectionApi.getDistance(directions))
    }

    override fun onPause() {
        super.onPause()
        interactiveDirectionsGenerator?.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        val requestionUpdates = interactiveDirectionsGenerator?.isRequestingLocationUpdates()
        if (requestionUpdates != null && requestionUpdates) {
            interactiveDirectionsGenerator?.startLocationUpdates()
        }
    }

    private fun setupLocation() {
       interactiveDirectionsGenerator = InteractiveDirectionsGenerator(this, mMap, directions)
    }

    private fun drawRoute() {
        val newLinePoints = directions.flatMap { direction -> direction.polyline.decodePath() }
        val newLine = makePolyline(newLinePoints)
        mMap.addPolyline(newLine)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()
        mMap.uiSettings.setZoomControlsEnabled(true)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(com.google.android.gms.maps.model.LatLng(startingPoint.lat, startingPoint.lng), 15.toFloat()))

        setupLocation()

        drawRoute()
    }

}

