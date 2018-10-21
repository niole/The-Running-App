package com.example.niolenelson.running.utilities

import com.google.maps.model.AutocompletePrediction

/**
 * Created by niolenelson on 10/20/18.
 */
class AutoCompleteViewDTO(val prediction: AutocompletePrediction) {
    override fun toString(): String {
        return prediction.description
    }
}

