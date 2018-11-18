package com.example.niolenelson.running.utilities

import android.app.Activity
import com.google.android.gms.maps.GoogleMap
import com.google.maps.model.LatLng

/**
 * When the user is on a segment of a path, that path is highlighted on the map
 */
class InteractiveDirectionsGenerator(val activity: Activity, mMap: GoogleMap, val directions: List<LocalDirectionApi.Direction>) {

    private val locationUpdater: LocationUpdater = LocationUpdater(activity, mMap)

    private val directionChunks: List<List<LatLng>> = directions.map { it.polyline.decodePath() }

    /**
     * communicates what direction in the route the user travels in
     * if 1, the user travels in the indexes positively
     * if -1, the user travels through the directions negatively
     * assumed to be positive to start
     * if the user decides to go the opposite way in the route, it will change
     */
    private var travelDirection: Int = 1

    /**
     * the index of the chunk direction where it was last determined that the
     * user is at
     * initialized once location starts to be reported
     */
    private var likelyChunkIndex: Int = -1

    /**
     * the index of the chunk where the user started their route
     * initialized after the user starts the run, will mostly likely be chunk 0
     */
    private var startChunkIndex: Int = -1

    /**
     * the index of the chunk which is the last chunk in the user's route
     * initialized after start chunk is determined
     */
    private var endChunkIndex: Int = -1

    init {
        locationUpdater.onLocationUpdate {
            lat, lng ->
            println("Latitude $lat")
            println("Longitude $lng")
        }
    }

    fun getDirectionsFromLocation(lat: Double, lng: Double) {
        // if two parts of the route are really close together, we can't have any
        // hopping between parts of the routes
        // what if a person gets off of the route but then gets back on the route?
        // lets deal with that later
        // if we're within a 5 meters of the current chunk perpendicularly, still in chunk
        // if within .12 miles of next chunk, announce
        val stillInChunk = true
        if (stillInChunk) {
            // check to see how far we are to next or previous chunk
        } else {
            // find out what chunk index we're at
        }
    }

    fun stopLocationUpdates() {
        locationUpdater.stopLocationUpdates()
    }

    fun startLocationUpdates() {
        locationUpdater.startLocationUpdates()
    }

    fun isRequestingLocationUpdates(): Boolean {
        return locationUpdater.isRequestingLocationUpdates()
    }

}