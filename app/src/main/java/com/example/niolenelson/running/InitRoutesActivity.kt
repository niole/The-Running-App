package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import com.example.niolenelson.running.utilities.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import java.lang.Double.parseDouble
import com.google.maps.model.AutocompletePrediction
import android.app.Activity
import android.view.KeyEvent
import java.util.*
import kotlin.concurrent.schedule

class AutoCompleteViewDTO(val prediction: AutocompletePrediction) {
    override fun toString(): String {
        return prediction.description
    }
}

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity :
        AppCompatActivity(),
        OnMapReadyCallback {

    private lateinit var selectedSuggestion: AutoCompleteViewDTO

    private lateinit var routeStartInput: ValidatedAutocompleteEditText

    var suggestions: MutableList<AutoCompleteViewDTO> = mutableListOf()

    private lateinit var typeaheadAdapter: ArrayAdapter<AutoCompleteViewDTO>

    private lateinit var mMap: GoogleMap

    private lateinit var geoContext: GeoApiContext

    private fun getDebouncer(context: Activity, cb: (keyCode: Int, event: KeyEvent) -> Unit): (view: View, keyCode: Int, event: KeyEvent) -> Boolean {
            var timer = false
            return {
                view: View, keyCode: Int, event: KeyEvent ->
                if (!timer) {
                    timer = true
                    Timer("SettingUp", false).schedule(500) {
                        timer = false
                        context.runOnUiThread {
                            cb(keyCode, event)
                        }
                    }
                }
                false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()

        typeaheadAdapter = ArrayAdapter(this, R.layout.location_suggestion_item, suggestions)

        routeStartInput = findViewById<ValidatedAutocompleteEditText>(R.id.route_start_input)
        routeStartInput.setAdapter(typeaheadAdapter)
        routeStartInput.threshold = 0

        routeStartInput.setOnItemClickListener {
           adapterView, view, index, id -> selectedSuggestion = suggestions[index]
        }

        routeStartInput.setOnKeyListener(getDebouncer(this, {
         keyCode: Int, event: KeyEvent ->
            val routeStartAddress = routeStartInput.text
            val predictions = PlacesApi.queryAutocomplete(geoContext, routeStartAddress.toString()).await()
            if (predictions != null) {
                suggestions.clear()
                suggestions.addAll(predictions.map { AutoCompleteViewDTO(it) } as ArrayList<AutoCompleteViewDTO>)
                typeaheadAdapter.clear()
                typeaheadAdapter.addAll(suggestions)
                typeaheadAdapter.notifyDataSetChanged()
            } else {
                println("no predictions")
            }
        }))

        routeStartInput.onFocusChangeListener = View.OnFocusChangeListener{
            view, focused ->
            if(focused) {
                // Display the suggestion dropdown on focus
                routeStartInput.showDropDown()
            }
        }

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
            val intent = Intent(this, MapsActivity::class.java)

            button.visibility = View.GONE
            UIUtilities.Spinner.add(this, R.id.init_routes_container)

            val routeLengthText: String = input_route_length.text.toString()
            val routeDistanceMiles = routeLengthText.toDouble()
            intent.putExtra("routeDistanceMiles", routeDistanceMiles)

            val routeStartAddress = routeStartInput.text
            if (selectedSuggestion == null) {
                // just go with route start address
                val predictions = PlacesApi.queryAutocomplete(geoContext, routeStartAddress.toString()).await()
                if (predictions != null && predictions.isNotEmpty()) {
                    val place = predictions[0]
                    val details = PlacesApi.placeDetails(geoContext, place.placeId).await()
                    val lat = details.geometry.location.lat
                    val lng = details.geometry.location.lng

                    intent.putExtra("starting_lat", lat)
                    intent.putExtra("starting_lng", lng)
                    startActivity(intent)
                } else {
                    // TODO you're screwed
                    println("You're screwed")
                }

            } else {
                // have selected item use that
                val suggestion = PlacesApi.placeDetails(geoContext, selectedSuggestion.prediction.placeId).await()
                intent.putExtra("starting_lat", suggestion.geometry.location.lat)
                intent.putExtra("starting_lng", suggestion.geometry.location.lng)
                startActivity(intent)
            }
        }
    }

}
