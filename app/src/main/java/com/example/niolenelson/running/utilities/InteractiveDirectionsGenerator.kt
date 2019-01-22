package com.example.niolenelson.running.utilities

import com.google.android.gms.maps.model.LatLng as BaseLatLng
import com.google.maps.model.LatLng

data class Instruction(
        val points: List<LatLng>,
        val humanDirections: String
)

open class InteractiveDirectionsGenerator(val locationUpdater: LocationUpdaterBase, val handleAnnoucement: (msg: String) -> Int, val stopAnnoucenemtns: () -> Unit, val onRouteEnd: () -> Unit, val directions: List<Instruction>) {

    private val minDiff: Double = 0.2

    private var announcedForChunks: MutableList<Boolean> = MutableList(directions.size) { false }

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
        locationUpdater.onLocationUpdate { lat, lng ->
            getDirectionsFromLocation(lat, lng)
        }
        locationUpdater.startLocationUpdates()
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
                onRouteEnd()
            }
        }
    }

    private fun doAnnounceDirections(lat: Double, lng: Double) {
        val currentChunk = directions[likelyChunkIndex]
        if ((likelyChunkIndex == 0 && !announcedForChunks[0]) || shouldDoAnnouncement(currentChunk.points.last(), lat, lng)) {
            // going forwards
            announcedForChunks[likelyChunkIndex] = true
            doFirstAnnouncement(likelyChunkIndex)
            likelyChunkIndex = Math.min(likelyChunkIndex + 1, directions.size - 1)
        }
    }

    private fun isRouteOver(lat: Double, lng: Double): Boolean {
        val currentChunk = directions[likelyChunkIndex]
        return shouldDoAnnouncement(currentChunk.points.last(), lat, lng)
    }

    private fun getCurrentChunkIndex(lat: Double, lng: Double): Int {
        if (likelyChunkIndex == -1) {
            // at start
            // determine current chunk, likely direction, start and end chunk
            // assume at chunk 0 at first and if not, search everywhere
            likelyChunkIndex = 0
            startChunkIndex = 0
            endChunkIndex = directions.size - 1
        }

        val likelyStartChunk = directions[likelyChunkIndex].points
        if (!locationInChunk(lat, lng, likelyStartChunk)) {

            // pick from one chunk previous just in case we moved forwards too soon
            val startIndex = Math.max(likelyChunkIndex - 1, 0)
            for (i in (startIndex until directions.size)) {
                val chunk = directions[i]
                if (locationInChunk(lat, lng, chunk.points)) {
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
        handleAnnoucement(directions[chunkIndex].humanDirections)
    }

    fun stopLocationUpdates() {
        locationUpdater.stopLocationUpdates()
        stopAnnoucenemtns()
    }

    fun startLocationUpdates() {
        locationUpdater.startLocationUpdates()
    }

    fun isRequestingLocationUpdates(): Boolean {
        return locationUpdater.isRequestingLocationUpdates()
    }
}