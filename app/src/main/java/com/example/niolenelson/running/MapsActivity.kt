package com.example.niolenelson.running

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Button
import android.widget.LinearLayout
import com.example.niolenelson.running.utilities.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.*
import com.google.maps.model.LatLng as JavaLatLng
import com.google.maps.DirectionsApi.newRequest
import com.google.maps.model.DistanceMatrix
import com.google.maps.model.DistanceMatrixRow
import com.google.maps.model.PlaceType
import com.google.maps.model.PlacesSearchResponse
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.Serializable

class MapsActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private val generatedRoutesAdapter = GeneratedRoutesAdapter(arrayListOf(), this)

    private val logger = logger()

    private lateinit var startingPoint: LatLng

    private var routeDistanceMiles: Double = 0.0

    private val routeError = .25

    private val uniquePointDistanceMiles = .5

    private lateinit var javaStartingPoint: JavaLatLng

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private var pathData: Array<JavaLatLng> = arrayOf()

    private var generatedRoutes: List<List<JavaLatLng>> = listOf()

    private var currentPolylines: Map<Int, Polyline> = mapOf()

    private var selectedRoute = -1

    private val nearbyPlacesCallback: APICallback<PlacesSearchResponse> = APICallback()

    lateinit private var routeGenerator: RouteGenerator

    init {
        nearbyPlacesCallback.addOnResult {
            searchResults ->

            searchResults.results.forEach {
                pathData = pathData.plus(it.geometry.location)
                this.runOnUiThread {
                    handlePathDataProcessing()
                }
            }
        }
        nearbyPlacesCallback.addOnFailure {
            error -> logger.warning("Error while getting nearby places for route creation. error: $error, message: ${error?.message}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()
        mMap.uiSettings.setZoomControlsEnabled(true)
        mMap.addMarker(MarkerOptions().position(startingPoint))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, 15.toFloat()))
    }

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
        generated_routes_list.adapter = generatedRoutesAdapter

        setStartingPoint(intent)

        setGeneratedRoutesData()

        setGetDirectionsButton()

        setCreateNextRouteButton()
    }

    private fun setCreateNextRouteButton() {
        val nextRouteButton = findViewById<Button>(R.id.next_route_button)
        nextRouteButton.setOnClickListener {
            println("sdlfkjsd")
            setNextRouteInSuggestionsList()
        }
    }

    private fun setGetDirectionsButton() {
        val getDirectionsButton = findViewById<Button>(R.id.get_directions_button)
        getDirectionsButton.setOnClickListener {
            if (selectedRoute > -1) {
                val result = newRequest(geoContext).origin(javaStartingPoint).destination(javaStartingPoint).waypoints(*generatedRoutes[selectedRoute].toTypedArray()).optimizeWaypoints(true).await()

                UIUtilities.Spinner.add(this, R.id.maps_activity_container)

                val intent = Intent(this, RouteActivity::class.java)

                intent.putExtra("startingPoint", javaStartingPoint as Serializable)
                intent.putExtra("directionsResult", result as Serializable)

                startActivity(intent)

            }
        }
    }

    private fun setStartingPoint(intent: Intent) {
        val lat: Double = intent.getDoubleExtra("starting_lat", 0.toDouble())
        val lng: Double = intent.getDoubleExtra("starting_lng", 0.toDouble())
        startingPoint = LatLng(lat, lng)
        javaStartingPoint = JavaLatLng(lat, lng)
    }

    /**
     * Gets a bunch of points and sets them on pathData
     */
    private fun generateRoutesData() {

        val nearbyPlacesRequest = PlacesApi.nearbySearchQuery(geoContext, javaStartingPoint)
        nearbyPlacesRequest
                .radius(Haversine.milesToMeters(routeDistanceMiles).toInt())
                .type(PlaceType.PARK)
                .setCallback(nearbyPlacesCallback)
    }

    private fun setGeneratedRoutesData() {
        launch(UI) {
            generateRoutesData()
        }
    }


    private fun handlePathDataProcessing() {
        prunePathData()

        // TODO make sure there is less than 26 pathData data points
       routeGenerator = RouteGenerator(1000.0, javaStartingPoint, Haversine.milesToMeters(routeDistanceMiles), geoContext, pathData)
    }


    private fun setNextRouteInSuggestionsList() {
       if (routeGenerator != null) {
           val nextRoute = routeGenerator.next()
           if (nextRoute.isNotEmpty()) {
               // could generate a route add to the suggestions array adapter
              val nextRoute = listOf(listOf(javaStartingPoint).plus(nextRoute.plus(javaStartingPoint)))
              generatedRoutes = generatedRoutes.plus(nextRoute)
              generatedRoutesAdapter.addItem()
           }

           if (!routeGenerator.hasMoreRoutes()) {
               // disable button and tell user
               logger.info("Oh no there are no more routes")
           }

       } else {
           logger.warning("Use is asking for routes before the route generator is initialized")
       }
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

        val pathDataValues = generatedRoutes[index]
        val newLine = mMap.addPolyline(RouteUtilities.makePolyline(pathDataValues))

        currentPolylines = currentPolylines.plus(Pair(index, newLine))
    }

    private fun prunePathData() {
        var keptPoints = arrayOf<JavaLatLng>()
        val prunedMap = pathData.filter {
            val closePoint = keptPoints.find { seenPoint: JavaLatLng ->
                getDistanceBetweenCoordinates(
                        seenPoint.lat,
                        it.lat,
                        seenPoint.lng,
                        it.lng
                ) <= uniquePointDistanceMiles
            }
            if (closePoint != null) {
                false
            } else {
                keptPoints = keptPoints.plus(it)
                true
            }
        }

       pathData = prunedMap.toTypedArray()
    }

    private fun getDistanceBetweenCoordinates(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {
        return Haversine.distanceMiles(lat1, lon1, lat2, lon2)
    }

    /**
     * returns distance between coords in a path in miles
     */
    private fun getPathDistance(path: List<JavaLatLng>): Double {
        var distance = 0.toDouble()
        if (path.size > 1) {
            for (i in 1..(path.size - 1)) {
                val curr = path[i]
                val prev = path[i - 1]
                distance += getDistanceBetweenCoordinates(
                        curr.lat,
                        prev.lat,
                        curr.lng,
                        prev.lng
                )
            }
        }

        return distance
    }

    /**
     * Goes over pathData and picks concentric circles of points that start and end
     * at the starting point
     */
    private fun getCircles(): List<List<JavaLatLng>> {
        var circles = listOf<List<JavaLatLng>>()
        val pathDataValues = pathData.toList()

        for (i in 0..(pathDataValues.size - 1)) {
            val startingPath = listOf(javaStartingPoint, pathDataValues[i])
            circles = circles.plus(
                buildCircles(
                        startingPath,
                        pathDataValues.take(i).plus(pathDataValues.drop(i + 1)),
                        getPathDistance(startingPath),
                        AngleGetter.getAngleFromNorthOnUnitCircle(javaStartingPoint)
                ).filter { it.size > 0 }
            )
        }

        return circles
    }

    /**
     * could take a point, could not take a point, could go back to start
     */
    private fun buildCircles(pickedPoints: List<JavaLatLng>, remainingPoints: List<JavaLatLng>, distance: Double, angleSoFar: Double): List<List<JavaLatLng>> {
        val angle = AngleGetter.getAngleFromNorthOnUnitCircle(pickedPoints.last())
        val totalAngle = (angle - angleSoFar) + angleSoFar
        if (totalAngle < Math.PI) {
            if (remainingPoints.isNotEmpty() && distance < routeDistanceMiles) {
                return remainingPoints.mapIndexed { index: Int, it: JavaLatLng ->
                    val remainder = remainingPoints.drop(index + 1)
                    return buildCircles(
                            pickedPoints.plus(it),
                            remainingPoints.take(index).plus(remainder),
                            distance + getPathDistance(listOf(pickedPoints.last(), it)),
                            totalAngle
                            )
                            .plus(buildCircles(pickedPoints, remainder, distance, angle))
                }
            }

            val finalDistance = distance + getPathDistance(listOf(pickedPoints.last(), javaStartingPoint))
            if (Math.abs(finalDistance - routeDistanceMiles) < routeError) {
                return listOf(pickedPoints.plus(javaStartingPoint))
            }
        }
        return listOf(listOf())
    }

}
