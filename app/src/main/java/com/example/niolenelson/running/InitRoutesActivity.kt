package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.example.niolenelson.running.utilities.ValidatedEditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.example.niolenelson.running.utilities.SelectableButton
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
        val spinner = findViewById<ProgressBar>(R.id.loadingPanel)
        button.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.init_routes_container ).removeView(spinner)
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
            val inflater = LayoutInflater.from(this)
            val spinner = inflater.inflate(R.layout.spinner, null, false)
            findViewById<LinearLayout>(R.id.init_routes_container ).addView(spinner)
            val text: String = input.text.toString()
            val routeDistanceMeters = text.toInt()
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("routeDistanceMiles", routeDistanceMeters)
            startActivity(intent)
        }

        if (!validateMiles(input.text.toString())) {
            button.disable()
        }

    }

}
