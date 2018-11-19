package com.example.niolenelson.running.utilities

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng as BaseLatLng
import com.google.android.gms.maps.GoogleMap
import com.google.maps.model.LatLng

/**
 * When the user is on a segment of a path, that path is highlighted on the map
 */
class InteractiveDirectionsGenerator(val activity: Activity, val mMap: GoogleMap, val directions: List<LocalDirectionApi.Direction>) {

    private val minDiff: Double = 0.05

    private val locationUpdater: LocationUpdater = LocationUpdater(activity, mMap)

    private val directionChunks: List<List<LatLng>> = directions.map { it.polyline.decodePath() }

    private var announcedForChunks: MutableList<Boolean> = MutableList(directionChunks.size) { false }

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
        //test()
        locationUpdater.onLocationUpdate {
            lat, lng ->
            getDirectionsFromLocation(lat, lng)
        }
    }

    fun getDirectionsFromLocation(lat: Double, lng: Double) {
        // if two parts of the route are really close together, we can't have any
        // hopping between parts of the routes
        // what if a person gets off of the route but then gets back on the route?
        // lets deal with that later
        // if we're within a 5 meters of the current chunk perpendicularly, still in chunk
        // if within tome diff miles of next chunk, announce
        likelyChunkIndex = getCurrentChunkIndex(lat, lng)
        if (likelyChunkIndex > -1) {
            doAnnounceDirections(lat, lng)
            if (isRouteOver(lat, lng)) {
                println("ROUTE OVER YAY")
                Toast.makeText(activity, "You did it!", Toast.LENGTH_LONG)
            }
        }
    }

    private fun doAnnounceDirections(lat: Double, lng: Double) {
        val currentChunk = directionChunks[likelyChunkIndex]
        if (shouldDoAnnouncement(currentChunk.last(), lat, lng)) {
            // going forwards
            announcedForChunks[likelyChunkIndex] = true
            doFirstAnnouncement(likelyChunkIndex)
            likelyChunkIndex = Math.min(likelyChunkIndex + 1, directionChunks.size - 1)
        }
    }

    private fun isRouteOver(lat: Double, lng: Double): Boolean {
        val currentChunk = directionChunks[likelyChunkIndex]
        return shouldDoAnnouncement(currentChunk.last(), lat, lng)
    }

    private fun getCurrentChunkIndex(lat: Double, lng: Double): Int {
        if (likelyChunkIndex == -1) {
            // at start
            // determine current chunk, likely direction, start and end chunk
            // assume at chunk 0 at first and if not, search everywhere
            likelyChunkIndex = 0
            startChunkIndex = 0
            endChunkIndex = directionChunks.size - 1
        }

        val likelyStartChunk = directionChunks[likelyChunkIndex]
        if (!locationInChunk(lat, lng, likelyStartChunk)) {

            // pick from one chunk previous just in case we moved forwards too soon
            val startIndex = Math.max(likelyChunkIndex - 1, 0)
            for (i in (startIndex until directionChunks.size)) {
                val chunk = directionChunks[i]
                if (locationInChunk(lat, lng, chunk)) {
                    // hooray
                    return i
                }
            }
        }
        return likelyChunkIndex
    }

    /**
     * determines whether lat lng pair is within .1 miles of chunk
     */
    private fun locationInChunk(lat: Double, lng: Double, chunk: List<LatLng>): Boolean {
        if (chunk.isNotEmpty()) {
            var point = chunk.first()
            if (chunk.size > 1) {
                point = chunk.reduce {
                    currentClosestPoint, nextPoint ->
                    val distanceToCurrent = getDistanceBetweenPoints(currentClosestPoint, lat, lng)
                    val distanceToNext = getDistanceBetweenPoints(nextPoint, lat, lng)
                    if (distanceToNext < distanceToCurrent) {
                        nextPoint
                    } else {
                        currentClosestPoint
                    }
                }
            }
            // TODO this could be better
            // currently just does distance to closest point
            // should do distance from route along perpendicular line
            val distance = getDistanceBetweenPoints(point, lat, lng)
            return distance <= minDiff
        }
        return false
    }

    private fun getDistanceBetweenPoints(basePoint: LatLng, lat: Double, lng: Double): Double {
        return Haversine.distanceMiles(lat, lng, basePoint.lat, basePoint.lng)
    }

    private fun shouldDoAnnouncement(endpoint: LatLng, lat: Double, lng: Double): Boolean {
        val nextDiff = getDistanceBetweenPoints(endpoint, lat, lng)
        val shouldAnnounce = nextDiff <= minDiff
        return shouldAnnounce && !announcedForChunks[likelyChunkIndex]
    }

    private fun doFirstAnnouncement(chunkIndex: Int) {
        println("ANNOUNCING $chunkIndex, ${directions[chunkIndex].htmlInstructions}")
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

    private fun test() {
        val points = directionChunks.flatten()
        var i = 0
        while (i < points.size) {
            val it = points[i]
            activity.runOnUiThread {
                getDirectionsFromLocation(it.lat, it.lng)
            }
            i += 1
        }
        println(directions.joinToString { "  |  ${it.htmlInstructions}" })
    }
}