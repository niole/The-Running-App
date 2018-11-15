package com.example.niolenelson.running.utilities

import android.support.v7.app.AppCompatActivity
import java.util.logging.Logger

fun AppCompatActivity.logger(): Logger {
    return Logger.getLogger(this.javaClass.name)
}

fun RandomRouteGenerator.logger(): Logger {
    return Logger.getLogger(this.javaClass.name)
}