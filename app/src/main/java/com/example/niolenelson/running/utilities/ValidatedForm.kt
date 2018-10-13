package com.example.niolenelson.running.utilities

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

/**
 * Created by niolenelson on 9/30/18.
 */

class ValidatedForm : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    var totalValidInputs = 0

    var totalValidationInputs: Int = 0

    var validationMap: Map<Integer, Boolean> = mapOf()

    lateinit var onSubmitHandler: (context: ValidatedForm) -> Unit

    lateinit var submitButton: SelectableButton

    private fun isValid(): Boolean {
        return totalValidInputs == totalValidationInputs
    }

    fun addOnSubmitListener(onSubmit: (context: ValidatedForm) -> Unit) {
        onSubmitHandler = onSubmit
    }

    fun setInputs(userSubmitButton: SelectableButton, vararg inputs: Triple<ValidatedInput, String, (String) -> Boolean>) {
        submitButton = userSubmitButton

        submitButton.setOnClickListener {
            view: View ->
            submitButton.visibility = View.GONE
            if (onSubmitHandler != null) {
                onSubmitHandler(this)
            }
        }

        totalValidationInputs = inputs.size
        inputs.forEach {
            val (input, errorMessage, validator) = it
            input.validator(
                    validator,
                    errorMessage,
                    {
                        val isValid = validationMap[input.getId() as Integer]
                        if (isValid != null) {
                            if (isValid) {
                                // if it was valid, it is now invalid
                                totalValidInputs -= 1
                            }
                        }
                        // set it
                        validationMap = validationMap.plus(Pair(input.getId() as Integer, false))
                        disableButton()
                },
                {
                    val isValid: Boolean? = validationMap[input.getId() as Integer]
                    if (isValid != null) {
                        if (!isValid) {
                            // if it was invalid, it is now valid
                            totalValidInputs += 1
                        }
                    } else {
                        // never was checked before
                        totalValidInputs += 1
                    }
                    // set it
                    validationMap = validationMap.plus(Pair(input.getId() as Integer, true))

                    tryToEnableButton()
                }
            )
        }

        if (!isValid()) {
            submitButton.disable()
        }
    }

    private fun disableButton() {
        if (!isValid()) {
            // enable the button
            val button = submitButton
            button.disable()
        }
    }

   private fun tryToEnableButton() {
       if (isValid()) {
           // enable the button
           val button = submitButton
           button.enable()
       }
   }
}
