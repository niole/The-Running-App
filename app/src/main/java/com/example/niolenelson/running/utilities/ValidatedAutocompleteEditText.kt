package com.example.niolenelson.running.utilities

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

/**
 * Created by niolenelson on 10/7/18.
 */
class ValidatedAutocompleteEditText<T> : AutoCompleteTextView, ValidatedInput {

    var selectedItem: T? = null

    var suggestions: MutableList<T> = mutableListOf()

    lateinit var typeaheadAdapter: ArrayAdapter<T>

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    init {
        this.onFocusChangeListener = View.OnFocusChangeListener{
            view, focused ->
            if(focused) {
                // Display the suggestion dropdown on focus
                this.showDropDown()
            }
        }

        this.setOnItemClickListener {
            adapterView, view, index, id -> selectedItem = suggestions[index]
        }

    }

    fun updateSuggestions(newSuggestions: ArrayList<T>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        typeaheadAdapter.clear()
        typeaheadAdapter.addAll(suggestions)
        typeaheadAdapter.notifyDataSetChanged()
    }

    fun initTypeahead(activity: Activity, layout: Int) {
        typeaheadAdapter = ArrayAdapter(activity, layout, suggestions)
        setAdapter(typeaheadAdapter)
        threshold = 0
    }

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
