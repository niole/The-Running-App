package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.niolenelson.running.utilities.ValidatedEditText
import com.example.niolenelson.running.utilities.SelectableButton
import com.example.niolenelson.running.utilities.UIUtilities
import java.lang.Double.parseDouble

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_routes)
        setButton()
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

    private fun setButton() {
        val button = findViewById<SelectableButton>(R.id.route_length_input_submit)
        val input = findViewById<ValidatedEditText>(R.id.route_length_input)

        input.validator(
                { s -> validateMiles(s) },
                "enter the number of miles for your route",
                { button.disable() },
                { button.enable() }
        )

        button.setOnClickListener {
            view: View ->
            button.visibility = View.GONE

            UIUtilities.Spinner.add(this, R.id.init_routes_container)

            val text: String = input.text.toString()
            val routeDistanceMiles = text.toDouble()
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("routeDistanceMiles", routeDistanceMiles)
            startActivity(intent)
        }

        if (!validateMiles(input.text.toString())) {
            button.disable()
        }

    }

}
