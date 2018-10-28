package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.niolenelson.running.utilities.*
import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import java.lang.Double.parseDouble
import android.widget.Toast
import com.google.maps.model.AutocompletePrediction
import com.google.maps.model.PlaceDetails
import java.util.*

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity :  AppCompatActivity() {
    private val searchedPhrases: MutableSet<String> = mutableSetOf()

    private val logger = logger()

    private val autocompleteCallback: APICallback<Array<AutocompletePrediction>> = APICallback()

    private val formSubmissionAutocompleteCallback: APICallback<Array<AutocompletePrediction>> = APICallback()

    private lateinit var routeStartInput: ValidatedAutocompleteEditText<AutoCompleteViewDTO>

    private lateinit var geoContext: GeoApiContext


    init {
        autocompleteCallback.addOnResult {
            predictions: Array<AutocompletePrediction> ->
            if (predictions.isNotEmpty()) {
                logger.info("Autocomplete query succeeded with predictions: $predictions")
                routeStartInput.updateSuggestions(predictions.map { AutoCompleteViewDTO(it) } as ArrayList<AutoCompleteViewDTO>)
            } else {
                logger.info("Autocomplete query succeeded, but there were no predictions")
            }
        }

        autocompleteCallback.addOnFailure {
            error: Throwable? ->
            logger.warning("Autocomplete query failed: $error")
        }
    }

    private fun initForm() {
        geoContext = GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build()

        routeStartInput = findViewById<ValidatedAutocompleteEditText<AutoCompleteViewDTO>>(R.id.route_start_input)

        routeStartInput.initTypeahead(this, R.layout.location_suggestion_item)

        routeStartInput.setDebouncedOnKeyListener(1000, this, {
            val routeStartAddress = routeStartInput.text.toString()
            if (!searchedPhrases.contains(routeStartAddress)) {
                searchedPhrases.add(routeStartAddress)
                PlacesApi.queryAutocomplete(geoContext, routeStartAddress).setCallback(autocompleteCallback)
            }
        })
        setForm()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_routes)
        initForm()
    }

    override fun onResume() {
        super.onResume()
        showButton()
    }

    private fun validateMiles(s: String): Boolean {
        var numeric = true
        try {
            parseDouble(s)
        } catch (e: Throwable) {
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

            formSubmissionAutocompleteCallback.addOnFailure {
               error ->
                logger.warning("Could not get predictions on form submission. error: $error")
                showButton()
                Toast.makeText(this, "Something went wrong when searching for $routeStartAddress. Please submit again or enter a new address", Toast.LENGTH_LONG)
            }
            formSubmissionAutocompleteCallback.addOnResult {
                predictions ->
                if (predictions != null && predictions.isNotEmpty()) {
                    val place = predictions[0]
                    val predictionDetailsCallback: APICallback<PlaceDetails> = APICallback()
                    predictionDetailsCallback.addOnResult {
                        details ->
                        logger.info("Starting activity with details: $details")
                        startMapsActivity(details.geometry.location.lat, details.geometry.location.lng)
                    }
                    predictionDetailsCallback.addOnFailure {
                        error ->
                            logger.warning("Failed to get details from prediction. Prediction: $place, error: $error")
                            showButton()
                            Toast.makeText(this, "Something went wrong when searching for $place. Please submit again or enter a new address", Toast.LENGTH_LONG)
                    }
                    PlacesApi.placeDetails(geoContext, place.placeId).setCallback(predictionDetailsCallback)
                } else {
                    // user entered a nonsensical place
                    // stop spinner
                    showButton()

                    // restart flow
                    logger.info("Could not find auto complete prediction for $routeStartAddress. Stopping form submission. predictions: $predictions")
                    Toast.makeText(this, "$routeStartAddress has no associated address. Enter a new starting point", Toast.LENGTH_LONG)
                }
            }

            if (routeStartInput.selectedItem == null) {
                // just go with route start address
                logger.info("No selected route start suggestion. Using $routeStartAddress")
                PlacesApi.queryAutocomplete(geoContext, routeStartAddress.toString()).setCallback(formSubmissionAutocompleteCallback)
            } else {
                // have selected item use that
                val selectedItem = routeStartInput.selectedItem as AutoCompleteViewDTO
                logger.info("Using user selected suggestion: $selectedItem")
                val placeDetailsCallback: APICallback<PlaceDetails> = APICallback()
                placeDetailsCallback.addOnFailure {
                    error -> logger.warning("Couldn't get place details for user selected suggestion. suggestion: $selectedItem, error: $error")
                }
                placeDetailsCallback.addOnResult {
                    suggestion ->
                    logger.info("Starting activity with details: $suggestion")
                    startMapsActivity(suggestion.geometry.location.lat, suggestion.geometry.location.lng)
                }

                PlacesApi.placeDetails(geoContext, selectedItem.prediction.placeId).setCallback(placeDetailsCallback)
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
        routeStartInput.clear()
        startActivity(intent)
    }

}
