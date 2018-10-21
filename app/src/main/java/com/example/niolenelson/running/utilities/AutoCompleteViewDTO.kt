package com.example.niolenelson.running.utilities

import com.google.maps.model.AutocompletePrediction

/**
 * Created by niolenelson on 10/20/18.
 */
data class AutoCompleteViewDTO(val prediction: AutocompletePrediction) {

    fun equals(other: AutoCompleteViewDTO?): Boolean {
        if (other != null) {
            return prediction.placeId == other.prediction.placeId
        }
        return false
    }

    override fun toString(): String {
        return prediction.description
    }
}

