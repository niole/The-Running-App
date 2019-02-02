package com.example.niolenelson.running.utilities

import com.google.android.gms.maps.model.LatLng as BaseLatLng
import com.google.maps.model.LatLng

/**
 * This takes the location of the user and announces a direction if necessary
 *
 * Don't want to announce the same directions twice
 * Want to announce the directions in order
 * Want to announce the directions when it makes sense
 *
 * This is done by
 * knowing what announcements have already happened
 * knowing what segment the user is closest to
 *
 * should take into account how large the current segment is so can find tune
 * Maybe should always pick a point to be within a certain distance of in order to
 * announce the next direction
 *
 * It's all about adapting it to what the location updater will do
 */

data class Instruction(
        val points: List<LatLng>,
        val humanDirections: String
)

data class CurrentLocation(
        val directionIndex: Int,
        val closestLatLngIndex: Int,
        val currentLatLng: LatLng,
        val instruction: Instruction
) {
    fun closeEnoughToAnnounce(minDiffMiles: Double): Boolean {
        val endPoint = instruction.points.last()
        return Haversine.distanceMiles(currentLatLng.lat, currentLatLng.lng, endPoint.lat, endPoint.lng) <= minDiffMiles
    }
}

open class InteractiveDirectionsGenerator(
        val locationUpdater: LocationUpdaterBase,
        val handleAnnoucement: (msg: String) -> Int,
        val stopAnnoucenemtns: () -> Unit,
        val onRouteEnd: () -> Unit,
        val directions: List<Instruction>
    ) {

    private var currentLocation: CurrentLocation = CurrentLocation(0, 0, directions[0].points[0], directions[0])

    private val minDiffMiles: Double = 0.05

    private var announcedForChunks: MutableList<Boolean> = MutableList(directions.size) { false }

    init {
        locationUpdater.onLocationUpdate { lat, lng ->
            getDirectionsFromLocation(lat, lng)
        }
        locationUpdater.startLocationUpdates()
    }


    fun getDirectionsFromLocation(lat: Double, lng: Double) {
        // need a current chunk index for expediency and future inference
        // also need the current location what we are closest to in the current chunk
        currentLocation = getNextLocation(lat, lng, currentLocation)
        doAnnounceDirections(currentLocation)
        if (isRouteOver()) {
            onRouteEnd()
        }
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

    /**
     * Always announce right at start
     */
    private fun doAnnounceDirections(currentLocation: CurrentLocation) {
        val shouldAnnounceForStart = !announcedForChunks[0] && currentLocation.directionIndex == 0
        if (shouldAnnounceForStart || (currentLocation.closeEnoughToAnnounce(minDiffMiles) && !announcedForChunks[currentLocation.directionIndex])) {
            announcedForChunks[currentLocation.directionIndex] = true
            handleAnnoucement(currentLocation.instruction.humanDirections)
        }
    }

    /**
     * Route is over if should do announcement for last chunk
     */
    private fun isRouteOver(): Boolean {
        return announcedForChunks[directions.size - 1]
    }

    private fun getNextLocation(currentLat: Double, currentLng: Double, previousLocation: CurrentLocation): CurrentLocation {
        val likelyCurrentStep = directions[previousLocation.directionIndex].points
        val likelyCurrentLatLng = likelyCurrentStep[previousLocation.closestLatLngIndex]
        val distanceToPreviousLocation = getMilesDistanceBetweenPoints(likelyCurrentLatLng, currentLat, currentLng)

        // check in whatever direction you want and if distance grows, user didn't go that way
        // if there is no closer point than current, then stay with current
        // check if they went forwards because directions only work if you're going forwards
        // zero in on closest location, but within reason

        // attempt forward search
        var closestDistance = distanceToPreviousLocation
        var closestLocation = previousLocation
        for (stepIndex in previousLocation.directionIndex..(directions.size - 1)) {
            var startLocationIndex = 0
            if (stepIndex == previousLocation.directionIndex) {
                startLocationIndex = previousLocation.closestLatLngIndex
            }

            val currentStepPoints = directions[stepIndex].points
            if (startLocationIndex < currentStepPoints.size - 1) { // if false, compare to next step's first index, TODO potentially you could always just miss an announcment completely
                for (i in (startLocationIndex + 1)..(currentStepPoints.size - 1)) {
                    if (isCloserLocation(currentLat, currentLng, currentStepPoints[i], closestLocation.currentLatLng, closestDistance)) {
                        closestLocation = CurrentLocation(stepIndex, i, currentStepPoints[i], directions[stepIndex])
                        closestDistance = getMilesDistanceBetweenPoints(currentStepPoints[i], currentLat, currentLng)
                    } else {
                        // TODO returning as soon as something is slightly longer than a previous reading is not perfect
                        // what if the user is on a windy path and their location isn't updating that often
                        // this current approach only works if the location updates often and the path isn't that windy
                        // probably should search more places but then infer that the user is in a certain place based on the assumption
                        // that the user goes forwards
                        return closestLocation
                    }
                }
            }
        }

        return closestLocation
    }

    private fun isCloserLocation(currentLat: Double, currentLng: Double, currentlyExaminedLocation: LatLng, currentlyDeterminedClosesLocation: LatLng, distanceToPreviousLocation: Double): Boolean {
        val distanceToCurrentLocation = getMilesDistanceBetweenPoints(currentlyExaminedLocation, currentLat, currentLng)
        val distanceToPreviousPossibleNewLocation = getMilesDistanceBetweenPoints(currentlyDeterminedClosesLocation, currentLat, currentLng)

        // only update index if distance less than previous location
        if (distanceToCurrentLocation < distanceToPreviousLocation) {
            // only keep going if distance to current location is less than the previous possible new location
            return distanceToPreviousPossibleNewLocation > distanceToCurrentLocation
        }
        return false
    }

    private fun getMilesDistanceBetweenPoints(basePoint: LatLng, lat: Double, lng: Double): Double {
        return Haversine.distanceMiles(lat, lng, basePoint.lat, basePoint.lng)
    }
}