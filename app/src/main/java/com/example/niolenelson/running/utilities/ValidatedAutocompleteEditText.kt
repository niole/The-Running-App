package com.example.niolenelson.running.utilities

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import android.widget.EditText
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.selects.whileSelect
import java.util.concurrent.TimeUnit


/**
 * Created by niolenelson on 10/7/18.
 */
class ValidatedAutocompleteEditText : AutoCompleteTextView, ValidatedInput {
    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    override fun validator(validate: (s: String) -> Boolean, errorMessage: String, onError: () -> Unit, onSuccess: () -> Unit) {
        this.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!validate(p0.toString())) {
                    error = errorMessage
                    onError()
                } else {
                    error = null
                    onSuccess()
                }
            }
        })

    }
}
