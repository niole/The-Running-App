package com.example.niolenelson.running

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.*
import com.google.maps.model.Bounds
import com.google.maps.model.ComponentFilter
import com.google.maps.model.LocationType
import com.google.maps.model.SnappedPoint
import com.google.maps.model.LatLng as JavaLatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polyline



class MapsActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private val startingPoint = LatLng(37.86612570,-122.25051598)

    private val routeDistanceMiles = 3

    private val routeError = .5

    private val javaStartingPoint = JavaLatLng(startingPoint.latitude, startingPoint.longitude)

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private lateinit var routeBounds: LatLngBounds

    private var pathData: Map<String, SnappedPoint> = mapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()

        mMap.addMarker(MarkerOptions().position(startingPoint).title("Home"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startingPoint))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.toFloat()))
        mMap.uiSettings.setZoomControlsEnabled(true)

        val point = javaStartingPoint
        val bounds = getBounds(point)
        val surroundingBounds = getSurroundingGridBounds(bounds.northeast, bounds.southwest)
        setPathDataInBounds(surroundingBounds)
        setPathDataInBounds(
                getSurroundingGridBounds(
                        JavaLatLng(routeBounds.northeast.latitude, routeBounds.northeast.longitude),
                        JavaLatLng(routeBounds.southwest.latitude, routeBounds.southwest.longitude)
                )
        )

        val pathDataValues = pathData.values

//        setMarkers(pathDataValues.toTypedArray(), true)

        googleMap.addPolyline(PolylineOptions()
                .add(*pathDataValues.map{ LatLng(it.location.lat, it.location.lng) }.toTypedArray()))
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
        return RoadsApi.snapToRoads(geoContext, true, northeast, southwest).await()
    }

    private fun setMarkers(points: Array<SnappedPoint>, color: Boolean = false) {
       for (point in points) {
           val m = MarkerOptions().position(LatLng(point.location.lat, point.location.lng)).title(point.placeId)
           if (color) {
               m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
           }
           mMap.addMarker(m)
       }
    }

//    private fun getDistancesForPoints() {
//        for (i in 0..(pathData.size - 1)) {
//            for (j in 0..(pathData.size - 1)) {
//                if (i != j) {
//
//                }
//            }
//        }
//       pathData
//        DistanceMatrixApiRequest.origins
//
//    }
}
