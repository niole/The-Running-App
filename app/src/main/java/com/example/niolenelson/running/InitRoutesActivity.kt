package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.niolenelson.running.utilities.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import java.lang.Double.parseDouble
import android.view.KeyEvent
import android.widget.Toast
import com.google.maps.PendingResult
import com.google.maps.model.AutocompletePrediction
import java.util.*

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private val autocompleteCallback: APICallback<Array<AutocompletePrediction>> = APICallback()

    private lateinit var routeStartInput: ValidatedAutocompleteEditText<AutoCompleteViewDTO>

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext


    init {
        autocompleteCallback.addOnResult {
            predictions: Array<AutocompletePrediction> ->
            if (predictions.isNotEmpty()) {
                routeStartInput.updateSuggestions(predictions.map { AutoCompleteViewDTO(it) } as ArrayList<AutoCompleteViewDTO>)
            } else {
                println("no predictions")
            }
        }

        autocompleteCallback.addOnFailure {
            error: Throwable? ->
            println(error)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()
        routeStartInput = findViewById<ValidatedAutocompleteEditText<AutoCompleteViewDTO>>(R.id.route_start_input)

        routeStartInput.initTypeahead(this, R.layout.location_suggestion_item)

        routeStartInput.setDebouncedOnKeyListener(1000, this, {
         keyCode: Int, event: KeyEvent ->
            val routeStartAddress = routeStartInput.text
            PlacesApi.queryAutocomplete(geoContext, routeStartAddress.toString()).setCallback(autocompleteCallback)
        })

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
        showButton()
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

        val form = findViewById<ValidatedForm>(R.id.init_routes_container)
        form.setInputs(button,
                    Triple(
                            input_route_length,
                            "enter the number of miles for your route",
                            { s -> validateMiles(s) }
                    ),
                    Triple(
                        routeStartInput,
            "where do you want to start?",
                        { s -> s.trim() != "" }
                    )
            )

        form.addOnSubmitListener {
            hideButton()

            val routeStartAddress = routeStartInput.text
            if (routeStartInput.selectedItem == null) {
                // just go with route start address
                val predictions = PlacesApi.queryAutocomplete(geoContext, routeStartAddress.toString()).await()
                if (predictions != null && predictions.isNotEmpty()) {
                    val place = predictions[0]
                    val details = PlacesApi.placeDetails(geoContext, place.placeId).await()
                    startMapsActivity(details.geometry.location.lat, details.geometry.location.lng)
                } else {
                    // user entered a nonsensical place
                    // stop spinner
                    showButton()

                    // restart flow
                    Toast.makeText(this, "$routeStartAddress has no associated address. Enter a new starting point", Toast.LENGTH_LONG)
                }

            } else {
                // have selected item use that
                val selectedItem = routeStartInput.selectedItem as AutoCompleteViewDTO
                val suggestion = PlacesApi.placeDetails(geoContext, selectedItem.prediction.placeId).await()
                startMapsActivity(suggestion.geometry.location.lat, suggestion.geometry.location.lng)
            }
        }
    }

    private fun hideButton() {
        val button = findViewById<SelectableButton>(R.id.route_length_input_submit)
        button.visibility = View.GONE
        UIUtilities.Spinner.add(this, R.id.init_routes_container)
    }

    private fun showButton() {
        val button = findViewById<SelectableButton>(R.id.route_length_input_submit)
        button.visibility = View.VISIBLE
        UIUtilities.Spinner.remove(this, R.id.init_routes_container)
    }

    private fun startMapsActivity(lat: Double, lng: Double) {
        val intent = Intent(this, MapsActivity::class.java)
        val routeLengthText: String = findViewById<ValidatedEditText>(R.id.route_length_input).text.toString()
        val routeDistanceMiles = routeLengthText.toDouble()
        intent.putExtra("routeDistanceMiles", routeDistanceMiles)
        intent.putExtra("starting_lat", lat)
        intent.putExtra("starting_lng", lng)
        startActivity(intent)
    }

}
