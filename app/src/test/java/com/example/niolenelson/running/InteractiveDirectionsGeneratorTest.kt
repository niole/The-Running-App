package com.example.niolenelson.running

import com.example.niolenelson.running.utilities.Instruction
import com.example.niolenelson.running.utilities.InteractiveDirectionsGenerator
import com.example.niolenelson.running.utilities.LocationUpdaterBase
import com.google.maps.model.LatLng
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InteractiveDikrectionsGeneratorTest {
    @Test
    fun shouldReturnFirstInstructionIfRightOnLocation() {
        val handleAnnouncement = { msg: String ->
            assertEquals("Announcement should be for first route instruction", "0.0", msg)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement,
                List(5, { index: Int -> index.toDouble() }).map { it: Double ->
                    Instruction(
                            listOf(LatLng(it, it)),
                            it.toString()
                    )
                }
        )
        locationUpdater.locationUpdateCallback(0.0, 0.0)
    }

    @Test
    fun shouldReturnThirdInstructionIfRightOnLocation() {
        val handleAnnouncement = { msg: String ->
            assertEquals("Announcement should be for third route instruction", "2.0", msg)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement,
                List(5, { index: Int -> index.toDouble() }).map { it: Double ->
                    Instruction(
                            listOf(LatLng(it, it)),
                            it.toString()
                    )
                })
        locationUpdater.locationUpdateCallback(2.0, 2.0)
    }

    fun setupAssertion(handleAnnouncement: (msg: String) -> Int, instructions: List<Instruction>): LocationUpdaterBase {
        class L : LocationUpdaterBase()
        val locationUpdater = L()

        InteractiveDirectionsGenerator(
                locationUpdater,
                handleAnnouncement,
                {},
                {},
                instructions
        )

        locationUpdater.startLocationUpdates()
        return locationUpdater
    }
}
