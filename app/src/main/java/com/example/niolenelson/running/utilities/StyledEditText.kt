package com.example.niolenelson.running.utilities

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout

open class StyledEditText : EditText {
    init {
        textSize = 22.toFloat()
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(5, 15, 5, 15)
    }
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)
}