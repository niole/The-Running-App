package com.example.niolenelson.running.utilities

import android.content.Context
import android.graphics.Color.*
import android.util.AttributeSet
import android.widget.Button

/**
 * Created by niolenelson on 7/29/18.
 */
class SelectableButton : Button {
    init {
        enable()
    }
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun disable(): Unit {
        isClickable = false
        setBackgroundColor(GRAY)
    }

    fun enable(): Unit {
        isClickable = true
        setTextColor(WHITE)
        setBackgroundColor(BLUE)
    }
}