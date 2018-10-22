package com.example.niolenelson.running.utilities

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by niolenelson on 10/7/18.
 */
class ValidatedAutocompleteEditText<T> : AutoCompleteTextView, ValidatedInput {

    var selectedItem: T? = null

    var suggestions: MutableList<T> = mutableListOf()

    var suggestionsSet: MutableMap<String, T> = mutableMapOf()

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
            adapterView, view, index, id ->
            selectedItem = typeaheadAdapter.getItem(index)
        }

    }

    fun clear() {
        selectedItem = null
        suggestionsSet.clear()
        suggestions.clear()
        typeaheadAdapter.clear()
    }

    private fun addNewSuggestions(newSuggestions: ArrayList<T>) {
        val nonRepeatedSuggestions = newSuggestions.filter { suggestionsSet.get(it.toString()) == null }
        nonRepeatedSuggestions.forEach {
            suggestionsSet.put(it.toString(), it)
        }
        suggestions.addAll(nonRepeatedSuggestions)
        typeaheadAdapter.addAll(nonRepeatedSuggestions)
    }

    fun updateSuggestions(newSuggestions: ArrayList<T>) {
        addNewSuggestions(newSuggestions)
        typeaheadAdapter.notifyDataSetChanged()
    }

    fun initTypeahead(activity: Activity, layout: Int) {
        typeaheadAdapter = ArrayAdapter(activity, layout, suggestions)
        setAdapter(typeaheadAdapter)
        threshold = 0
    }

    fun setDebouncedOnKeyListener(delay: Long, context: Activity, cb: (keyCode: Int, event: KeyEvent) -> Unit): Unit {
        this.setOnKeyListener(getDebouncer(delay, context , cb))
    }

    private fun getDebouncer(delay: Long, context: Activity, cb: (keyCode: Int, event: KeyEvent) -> Unit): (view: View, keyCode: Int, event: KeyEvent) -> Boolean {
        var timer = false
        return {
            view: View, keyCode: Int, event: KeyEvent ->

            if (selectedItem != null) {
                selectedItem = null
            }

            if (!timer) {
                timer = true
                Timer("ValidatedAutocompletedEditTextDebouncer", false).schedule(delay) {
                    timer = false
                    context.runOnUiThread {
                        cb(keyCode, event)
                    }
                }
            }
            false
        }
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
