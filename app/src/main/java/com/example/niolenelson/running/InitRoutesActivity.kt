package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.niolenelson.running.utilities.ValidatedEditText
import com.example.niolenelson.running.utilities.SelectableButton
import com.example.niolenelson.running.utilities.UIUtilities
import com.example.niolenelson.running.utilities.ValidatedForm
import java.lang.Double.parseDouble

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_routes)
        setForm()
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

            println(routeStartAddress)

            // TODO get lat lng of start address
            // TODO validate address
            // TODO center inputs in init routes view
            intent.putExtra("starting_lat", 37.872983)
            intent.putExtra("starting_lng", -122.255754)

            startActivity(intent)
        }
    }

}
