package com.example.niolenelson.running.utilities

import android.app.Activity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.example.niolenelson.running.R


/**
 * Created by niolenelson on 9/22/18.
 */

object UIUtilities {
    object Spinner {
        fun add(context: Activity, viewId: Int) {
            val inflater = LayoutInflater.from(context)
            val spinner = inflater.inflate(R.layout.spinner, null, false)
            context.findViewById<LinearLayout>(viewId).addView(spinner)
        }

        fun remove(context: Activity, viewId: Int) {
            val spinner = context.findViewById<ProgressBar>(R.id.loadingPanel)
            context.findViewById<LinearLayout>(viewId).removeView(spinner)
        }
    }
}