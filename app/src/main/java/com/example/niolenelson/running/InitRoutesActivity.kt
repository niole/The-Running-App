package com.example.niolenelson.running

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText

/**
 * Created by niolenelson on 7/29/18.
 */
class InitRoutesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_routes)
        setButton()
    }

    private fun setButton() {
        val button = findViewById<Button>(R.id.route_length_input_submit)
        button.setOnClickListener {
            view: View ->
            // TODO validate input
            val text: String = findViewById<EditText>(R.id.route_length_input).text.toString()
            val routeDistanceMeters = text.toInt()
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("routeDistanceMiles", routeDistanceMeters)
            startActivity(intent)
        }
    }

}
