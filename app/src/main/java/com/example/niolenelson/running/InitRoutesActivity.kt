package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar

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
        val button = findViewById<Button>(R.id.route_length_input_submit)
        val spinner = findViewById<ProgressBar>(R.id.loadingPanel)
        button.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.init_routes_container ).removeView(spinner)
    }


    private fun setButton() {
        val button = findViewById<Button>(R.id.route_length_input_submit)
        button.setOnClickListener {
            view: View ->
            // TODO validate input
            button.visibility = View.GONE
            val inflater = LayoutInflater.from(this)
            val spinner = inflater.inflate(R.layout.spinner, null, false)
            findViewById<LinearLayout>(R.id.init_routes_container ).addView(spinner)
            val text: String = findViewById<EditText>(R.id.route_length_input).text.toString()
            val routeDistanceMeters = text.toInt()
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("routeDistanceMiles", routeDistanceMeters)
            startActivity(intent)
        }
    }

}
