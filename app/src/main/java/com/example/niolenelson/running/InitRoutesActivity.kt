package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.niolenelson.running.utilities.ValidatedEditText
import com.example.niolenelson.running.utilities.SelectableButton
import com.example.niolenelson.running.utilities.UIUtilities
import com.example.niolenelson.running.utilities.ValidatedForm
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import java.lang.Double.parseDouble

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()

        setForm()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_routes)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        val button = findViewById<SelectableButton>(R.id.route_length_input_submit)
        button.visibility = View.VISIBLE
        UIUtilities.Spinner.remove(this, R.id.init_routes_container)
    }

    private fun validateMiles(s: String): Boolean {
        var numeric = true
        try {
            parseDouble(s)
        } catch (e: NumberFormatException) {
            numeric = false
        }
        return numeric
    }

    private fun setForm() {
        val button = findViewById<SelectableButton>(R.id.route_length_input_submit)
        val input_route_length = findViewById<ValidatedEditText>(R.id.route_length_input)
        val route_start_input = findViewById<ValidatedEditText>(R.id.route_start_input)

        val form = findViewById<ValidatedForm>(R.id.init_routes_container)
        form.setInputs(button,
                    Triple(
                            input_route_length,
                            "enter the number of miles for your route",
                            { s -> validateMiles(s) }
                    ),
                    Triple(
                        route_start_input,
            "where do you want to start?",
                        { s -> s.trim() != "" }
                    )
            )

        form.addOnSubmitListener {
            val intent = Intent(this, MapsActivity::class.java)

            button.visibility = View.GONE
            UIUtilities.Spinner.add(this, R.id.init_routes_container)

            val routeLengthText: String = input_route_length.text.toString()
            val routeDistanceMiles = routeLengthText.toDouble()
            intent.putExtra("routeDistanceMiles", routeDistanceMiles)

            val routeStartAddress = route_start_input.text

            val res = PlacesApi.queryAutocomplete(geoContext, routeStartAddress.toString()).await()

            if (res.size > 0) {
                // TODO do this as a dropdown with autocomplete
                // make this async
                val place = res[0]
                val details = PlacesApi.placeDetails(geoContext, place.placeId).await()
                val lat = details.geometry.location.lat
                val lng = details.geometry.location.lng

                // TODO get lat lng of start address
                // TODO validate address
                // TODO center inputs in init routes view
                intent.putExtra("starting_lat", lat)
                intent.putExtra("starting_lng", lng)

            } else {
                intent.putExtra("starting_lat", 37.872983)
                intent.putExtra("starting_lng", -122.255754)
            }

            startActivity(intent)
        }
    }

}
