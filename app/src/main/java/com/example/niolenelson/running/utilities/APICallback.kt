package com.example.niolenelson.running.utilities

import com.google.maps.PendingResult

/**
 * Created by niolenelson on 10/13/18.
 */
class APICallback<T> : PendingResult.Callback<T> {

    private lateinit var onResultCB: (result: T) -> Unit

    private lateinit var onFailureCB: (e: Throwable?) -> Unit

    fun addOnResult(cb: (result: T) -> Unit) {
        onResultCB = cb
    }

    fun addOnFailure(cb: (e: Throwable?) -> Unit) {
        onFailureCB = cb
    }

    override fun onResult(result: T) {
        if (onResultCB != null) {
            onResultCB(result)
        }
    }

    override fun onFailure(e: Throwable?) {
        if (onFailureCB != null) {
            onFailureCB(e)
        }
    }
}
