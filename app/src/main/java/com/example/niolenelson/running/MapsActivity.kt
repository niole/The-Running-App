package com.example.niolenelson.running

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.LinearLayout
import com.example.niolenelson.running.utilities.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.*
import com.google.maps.model.Bounds
import com.google.maps.model.SnappedPoint
import com.google.maps.model.LatLng as JavaLatLng
import com.google.maps.DirectionsApi.newRequest
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.Serializable

class MapsActivity :
        AppCompatActivity(),
        OnMapReadyCallback {
    private val startingPoint = LatLng(37.86612570,-122.25051598)

    private var routeDistanceMiles: Double = 0.0

    private val routeError = .5

    private val uniquePointDistanceKm = .5

    private val javaStartingPoint = JavaLatLng(startingPoint.latitude, startingPoint.longitude)

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private lateinit var routeBounds: LatLngBounds

    private var pathData: Map<String, SnappedPoint> = mapOf()

    private var generatedRoutes: List<List<JavaLatLng>> = listOf()

    private var currentPolylines: Map<Int, Polyline> = mapOf()

    private var selectedRoute = -1

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

        setGeneratedRoutesData(this.generated_routes_list)
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

    private fun generateRoutesData() {
        val totalBoundFinds = Math.ceil(routeDistanceMiles / 2).toInt()
        val point = javaStartingPoint
        val bounds = getBounds(point)
        val surroundingBounds = getSurroundingGridBounds(bounds.northeast, bounds.southwest)

        if (totalBoundFinds >= 1) {
            setPathDataInBounds(surroundingBounds)
        }

        for (i in 1..(totalBoundFinds - 1)) {
            setPathDataInBounds(
                    getSurroundingGridBounds(
                            JavaLatLng(routeBounds.northeast.latitude, routeBounds.northeast.longitude),
                            JavaLatLng(routeBounds.southwest.latitude, routeBounds.southwest.longitude)
                    )
            )
        }
    }

    private fun setGeneratedRoutesData(generated_routes_list: RecyclerView) {
        val activity = this
        launch(UI) {
            generateRoutesData()
            prunePathData()
            val routes: List<List<JavaLatLng>> = getCircles()
            generatedRoutes  = routes
            generated_routes_list.adapter = GeneratedRoutesAdapter(generatedRoutes as ArrayList<com.google.maps.model.LatLng>, activity)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()
        mMap.addMarker(MarkerOptions().position(startingPoint).title("Home"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startingPoint))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.toFloat()))
        mMap.uiSettings.setZoomControlsEnabled(true)
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
        var keptPoints = arrayOf<SnappedPoint>()
        val prunedMap = pathData.filterValues {
            val closePoint = keptPoints.find { seenPoint: SnappedPoint ->
                getDistanceBetweenCoordinates(
                        seenPoint.location.lat,
                        it.location.lat,
                        seenPoint.location.lng,
                        it.location.lng
                ) <= uniquePointDistanceKm
            }
            if (closePoint != null) {
                false
            } else {
                keptPoints = keptPoints.plus(it)
                true
            }
        }

       pathData = prunedMap
    }

    private fun setPathDataInBounds(surroundingBounds: Array<LatLngBounds>) {
        val markerAddresses: Array<SnappedPoint> = (surroundingBounds.flatMap {
            getPointsInBounds(
                    JavaLatLng(it.northeast.latitude, it.northeast.longitude),
                    JavaLatLng(it.southwest.latitude, it.southwest.longitude)
            ).asIterable()
        }).toTypedArray()

        markerAddresses.forEach {
            pathData = pathData.plus(Pair(it.placeId, it))
        }
    }

    private fun getDistanceBetweenCoordinates(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {
        return Haversine.distance(lat1, lon1, lat2, lon2, 'K') * 1.609
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
     * Goes over Array<SnappedPoint> and picks concentric circles of points that start and end
     * at the starting point
     */
    private fun getCircles(): List<List<JavaLatLng>> {
        var circles = listOf<List<JavaLatLng>>()
        val pathDataValues = pathData.values.toList()

        for (i in 0..(pathDataValues.size - 1)) {
            val startingPath = listOf(javaStartingPoint, pathDataValues[i].location)
            circles = circles.plus(
                buildCircles(
                        startingPath,
                        pathDataValues.take(i).plus(pathDataValues.drop(i + 1)).map { it.location },
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

            if (Math.abs(distance - routeDistanceMiles) < routeError) {
                return listOf(pickedPoints.plus(javaStartingPoint))
            }
        }
        return listOf(listOf())
    }

    /**
     * get surrounding 8 squares that surround original bounding area
     */
    private fun getSurroundingGridBounds(northeast: JavaLatLng, southwest: JavaLatLng): Array<LatLngBounds> {
        val squareHeight = Math.abs(northeast.lat - southwest.lat)
        val squareWidth = Math.abs(northeast.lng - southwest.lng)

        val topSquareSW = LatLng(northeast.lat, southwest.lng)
        val topSquareNE = LatLng(northeast.lat + squareHeight, northeast.lng)
        val topSquare = LatLngBounds(topSquareSW, topSquareNE)

        val topRightSquare = LatLngBounds(
                LatLng(topSquareSW.latitude, topSquareSW.longitude + squareWidth),
                LatLng(topSquareNE.latitude, topSquareSW.longitude + squareWidth)
        )

        val topLeftSquare = LatLngBounds(
                LatLng(topSquareSW.latitude, topSquareSW.longitude - squareWidth),
                LatLng(topSquareNE.latitude, topSquareSW.longitude - squareWidth)
        )

        val leftSquare = LatLngBounds(
                LatLng(southwest.lat, southwest.lng - squareWidth),
                LatLng(northeast.lat, northeast.lng - squareWidth)
        )

        val rightSquare = LatLngBounds(
                LatLng(southwest.lat, southwest.lng + squareWidth),
                LatLng(northeast.lat, northeast.lng + squareWidth)
        )

        val bottomMiddleSquare = LatLngBounds(
                LatLng(southwest.lat - squareHeight, southwest.lng),
                LatLng(northeast.lat - squareHeight, northeast.lng)
        )

        val bottomRightSquare = LatLngBounds(
                LatLng(southwest.lat - squareHeight, southwest.lng + squareWidth),
                LatLng(northeast.lat - squareHeight, northeast.lng + squareWidth)
        )

        val bottomLeftSquare = LatLngBounds(
                LatLng(southwest.lat - squareHeight, southwest.lng - squareWidth),
                LatLng(northeast.lat - squareHeight, northeast.lng - squareWidth)
        )

        routeBounds = LatLngBounds(
            bottomLeftSquare.southwest,
            topRightSquare.northeast
        )

        return arrayOf(
                topSquare,
                topRightSquare,
                topLeftSquare,
                leftSquare,
                rightSquare,
                bottomMiddleSquare,
                bottomRightSquare,
                bottomLeftSquare
        )
    }

    private fun getBounds(latLng: JavaLatLng): Bounds {
        val address = GeocodingApi.reverseGeocode(geoContext, latLng).await()
        return address[0].geometry.viewport
    }

    private fun getPointsInBounds(northeast: JavaLatLng, southwest: JavaLatLng): Array<SnappedPoint> {
        val points = RoadsApi.snapToRoads(geoContext, true, northeast, southwest).await()
        return points ?: arrayOf()
    }

}
