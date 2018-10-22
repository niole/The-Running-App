package com.example.niolenelson.running.utilities

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout

open class StyledEditText : EditText {
    constructor(context: Context): super(context) {
       setStyles()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setStyles()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        setStyles()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        setStyles()
    }

    private fun setStyles() {
        textSize = 22.toFloat()
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(5, 15, 5, 15)
    }

}