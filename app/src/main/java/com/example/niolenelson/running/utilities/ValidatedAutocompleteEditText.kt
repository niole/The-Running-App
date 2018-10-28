package com.example.niolenelson.running.utilities

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Created by niolenelson on 10/7/18.
 */
class ValidatedAutocompleteEditText<T> : StyledAutocompleteEditText, ValidatedInput {

    private var debouncedTextChangeCallback: () -> Boolean = { false }

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

    fun setDebouncedOnKeyListener(delay: Long, context: Activity, cb: () -> Unit) {
        debouncedTextChangeCallback = getDebouncer(delay, context, cb)
    }

    private fun getDebouncer(delay: Long, context: Activity, cb: () -> Unit): () -> Boolean {
        var timer = false
        return {
            if (selectedItem != null) {
                selectedItem = null
            }

            if (!timer) {
                timer = true
                Executors.newSingleThreadScheduledExecutor().schedule({
                    timer = false
                    context.runOnUiThread {
                        cb()
                    }
                }, delay, TimeUnit.MILLISECONDS)

            }
            true
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
                    debouncedTextChangeCallback()
                    onSuccess()
                }
            }
        })

    }
}
