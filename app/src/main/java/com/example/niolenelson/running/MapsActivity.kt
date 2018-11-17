package com.example.niolenelson.running

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.example.niolenelson.running.utilities.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.*
import com.google.maps.model.LatLng as JavaLatLng
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.Serializable

class MapsActivity :
        AppCompatActivity(),
        OnMapReadyCallback {
    private lateinit var startingPoint: LatLng

    private var routeDistanceMiles: Double = 0.0

    private lateinit var javaStartingPoint: JavaLatLng

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private var currentPolylines: Map<Int, Polyline> = mapOf()

    private var selectedRoute = -1

    private lateinit var routeGenerator: RandomRouteGenerator

    override fun onResume() {
        super.onResume()
        UIUtilities.Spinner.remove(this, R.id.maps_activity_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        findViewById<SelectableButton>(R.id.get_directions_button).disable()

        routeDistanceMiles = intent.getDoubleExtra("routeDistanceMiles", 3.0)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayout.HORIZONTAL
        this.generated_routes_list.layoutManager = linearLayoutManager

        setStartingPoint(intent)

        val getDirectionsButton = findViewById<Button>(R.id.get_directions_button)
        getDirectionsButton.setOnClickListener {
            if (selectedRoute > -1) {
                val result = routeGenerator.getRouteAtIndex(selectedRoute).directionsSoFar

                UIUtilities.Spinner.add(this, R.id.maps_activity_container)

                val intent = Intent(this, RouteActivity::class.java)

                intent.putExtra("startingPoint", javaStartingPoint as Serializable)
                intent.putExtra("directionsResult", result as Serializable)

                startActivity(intent)

            }
        }
       findViewById<Button>(R.id.get_next_button).setOnClickListener {
          setNextRoute()
       }
    }

    private fun setNextRoute() {
        val button = findViewById<SelectableButton>(R.id.get_next_button)
        val that = this
        button.disable()
        UIUtilities.Spinner.add(that, R.id.maps_activity_container)
        launch(UI) {
            val nextRoute: Route? = routeGenerator.next()
            button.enable()
            UIUtilities.Spinner.remove(that, R.id.maps_activity_container)

            if (nextRoute != null) {
                if (generated_routes_list.adapter == null) {
                    generated_routes_list.adapter = GeneratedRoutesAdapter(routeGenerator.routes, that)
                } else {
                    generated_routes_list.adapter.notifyDataSetChanged()
                }
            } else {
                Toast.makeText(that, "Couldn\'t generate route. Try again.", Toast.LENGTH_LONG)
            }
        }
    }

    private fun setStartingPoint(intent: Intent) {
        val lat: Double = intent.getDoubleExtra("starting_lat", 0.toDouble())
        val lng: Double = intent.getDoubleExtra("starting_lng", 0.toDouble())
        startingPoint = LatLng(lat, lng)
        javaStartingPoint = JavaLatLng(lat, lng)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()
        mMap.uiSettings.setZoomControlsEnabled(true)
        mMap.addMarker(MarkerOptions().position(startingPoint))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, 15.toFloat()))

        routeGenerator = RandomRouteGenerator(geoContext, javaStartingPoint, routeDistanceMiles)
    }

    fun removeRouteAtIndex(index: Int) {
        val line = currentPolylines.get(index)
        if (line != null) {
            line.remove()
            currentPolylines = currentPolylines.minus(index)
        }
    }

    fun selectRouteAtIndex(index: Int) {
        if (selectedRoute == -1) {
            findViewById<SelectableButton>(R.id.get_directions_button).enable()
        }
        selectedRoute = index

        val route = routeGenerator.getRouteAtIndex(index).directionsSoFar
        if (route != null) {
            val pathDataValues = route.routes[0].overviewPolyline
            val newLine = mMap.addPolyline(RouteUtilities.makePolyline(pathDataValues.decodePath()))
            currentPolylines = currentPolylines.plus(Pair(index, newLine))
        }
    }

}
