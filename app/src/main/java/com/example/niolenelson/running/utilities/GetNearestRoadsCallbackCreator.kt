package com.example.niolenelson.running.utilities
import com.google.maps.PendingResult
import com.google.maps.model.SnappedPoint

class GetNearestRoadsCallback(
        onSuccess: (point: Array<SnappedPoint>) -> Void,
        onFailure: (e: Throwable) -> Void): PendingResult.Callback<Array<SnappedPoint>> {
    private val successCallback = onSuccess
    private val failureCallback = onFailure

    override fun onResult(point: Array<SnappedPoint>) {
        successCallback(point)
    }

    override fun onFailure(e: Throwable) {
        failureCallback(e)
    }
}