package com.example.niolenelson.running.utilities

/**
 * Created by niolenelson on 10/7/18.
 */

interface ValidatedInput {
    fun getId(): Int
    fun validator(validate: (s: String) -> Boolean, errorMessage: String, onError: () -> Unit, onSuccess: () -> Unit)
}