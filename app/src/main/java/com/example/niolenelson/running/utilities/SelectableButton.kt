package com.example.niolenelson.running.utilities

import android.content.Context
import android.graphics.Color.*
import android.util.AttributeSet
import android.widget.Button
import com.example.niolenelson.running.R

/**
 * Created by niolenelson on 7/29/18.
 */
class SelectableButton : Button {
    init {
        enable()
        setPadding(20, 0, 20, 0)
    }
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun disable() {
        isClickable = false
        setTextColor(resources.getColor(R.color.colorAccent))
        setBackgroundColor(WHITE)
    }

    fun enable() {
        isClickable = true
        setTextColor(WHITE)
        setBackgroundColor(resources.getColor(R.color.colorPrimary))
    }
}