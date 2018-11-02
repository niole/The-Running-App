package com.example.niolenelson.running.utilities

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.niolenelson.running.R


/**
 * Created by niolenelson on 9/22/18.
 */

object UIUtilities {
    object Spinner {
        fun add(context: Activity, view: ViewGroup) {
            val inflater = LayoutInflater.from(context)
            val spinner = inflater.inflate(R.layout.spinner, null, false)
            view.addView(spinner)
        }

        fun remove(context: Activity, view: ViewGroup) {
            val spinner = context.findViewById<ProgressBar>(R.id.loadingPanel)
            view.removeView(spinner)
        }
    }
}