package com.example.niolenelson.running

import com.example.niolenelson.running.utilities.Haversine
import com.example.niolenelson.running.utilities.Instruction
import com.example.niolenelson.running.utilities.InteractiveDirectionsGenerator
import com.example.niolenelson.running.utilities.LocationUpdaterBase
import com.google.maps.model.LatLng
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance



@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InteractiveDikrectionsGeneratorTest {
    @Test
    fun should_always_announce_first_instruction_immediately() {
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)

        var actual = ""
        val handleAnnouncement = { msg: String ->
            actual = msg
            1
        }

        val locationUpdater = setupAssertion(handleAnnouncement, createInstructions(steps))

        val firstStep = steps.first()
        val firstInstruction = firstStep.first()

        locationUpdater.locationUpdateCallback(firstInstruction.lat, firstInstruction.lng)
        assertEquals("0", actual, "Should announce if right at last location")
    }

    @Test
    fun should_not_announce_first_instruction_if_at_start_of_longer_route_and_not_close_to_start() {
        val expected = "no announcement"
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)

        var actual = expected
        val handleAnnouncement = { msg: String ->
            actual = msg
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement, createInstructions(steps))
        val thirdStep = steps[1]
        val firstInstruction = thirdStep[2]
        locationUpdater.locationUpdateCallback(firstInstruction.lat, firstInstruction.lng)

        assertEquals(expected, actual, "Should not announce if at beginning of step and step is long")
    }

    @Test
    fun should_announce_first_instruction_if_nearing_end_of_route() {
        val expected = "1"
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)

        var actual = ""
        val handleAnnouncement = { msg: String ->
            actual = msg
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement, createInstructions(steps))
        val step = steps[1]
        val firstInstruction = step[7]
        locationUpdater.locationUpdateCallback(firstInstruction.lat, firstInstruction.lng)

        assertEquals(expected, actual, "Should announce if at nearing end of step")
    }

    @Test
    fun should_not_announce_21_instruction_if_in_middle_of_longer_route() {
        val expected = "no announcement"
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)

        var actual = expected
        val handleAnnouncement = { msg: String ->
            println("message: $msg")
            actual = msg
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement, createInstructions(steps))
        val step = steps[21]
        val firstInstruction = step[4]
        for (i in 0..(steps.size - 1)) {
            println(i)
            locationUpdater.locationUpdateCallback(steps[i].last().lat, steps[i].last().lng)
        }

        assertEquals(expected, actual, "Should not announce if at beginning of step and step is long")
    }

    @Test
    fun shouldReturnFirstInstructionEvenIfNotOnPath15() {
        val handleAnnouncement = { msg: String ->
            assertEquals("Announcement should be for first route instruction", "0.0", msg)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement,
                List(5, { index: Int -> index.toDouble() }).map { it: Double ->
                    val base = it * 5
                    val subSteps = List(5, { it.toDouble() }).map {LatLng(base + it, base + it)}
                    Instruction(subSteps, base.toString())
                })
        locationUpdater.locationUpdateCallback(1.5, 1.5)
    }

    @Test
    fun shouldReturnFirstInstructionEvenIfNotOnPath115() {
        val handleAnnouncement = { msg: String ->
            assertEquals("Announcement should be for first route instruction", "0.0", msg)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement,
                List(5, { index: Int -> index.toDouble() }).map { it: Double ->
                    val base = it * 5
                    val subSteps = List(5, { it.toDouble() }).map {LatLng(base + it, base + it)}
                    Instruction(subSteps, base.toString())
                })
        locationUpdater.locationUpdateCallback(1.0, 1.5)
    }

    @Test
    fun shouldReturnFirstInstructionEvenIfNotOnPath12() {
        val handleAnnouncement = { msg: String ->
            assertEquals("Announcement should be for first route instruction", "0.0", msg)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement,
                List(5, { index: Int -> index.toDouble() }).map { it: Double ->
                    val base = it * 5
                    val subSteps = List(5, { it.toDouble() }).map {LatLng(base + it, base + it)}
                    Instruction(subSteps, base.toString())
                })
        locationUpdater.locationUpdateCallback(1.0, 2.0)
    }

    @Test
    fun shouldReturnThirdInstructionEvenIfNotOnPath() {
        val handleAnnouncement = { msg: String ->
            assertEquals("Announcement should be for first route instruction", "10.0", msg)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement,
                List(5, { index: Int -> index.toDouble() }).map { it: Double ->
                    val base = it * 5
                    val subSteps = List(5, { it.toDouble() }).map {LatLng(base + it, base + it)}
                    Instruction(subSteps, base.toString())
                })
        val x = Haversine.getLocationXDistanceFromLocationKM(10.0, 10.0, .16, 90.0)

        locationUpdater.locationUpdateCallback(x.lat, x.lng)
    }

    @Test
    fun shouldNotAnnounceSameDirectionTwice() {
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)
        var count = 0
        val handleAnnouncement = { _: String ->
            count += 1
            assertEquals( 1, count)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement, createInstructions(steps))

        val instruction = steps[0]
        val location = instruction[0]

        val x = Haversine.getLocationXDistanceFromLocationKM(location.lat, location.lng, .16, 90.0)

        locationUpdater.locationUpdateCallback(x.lat, x.lng)
        locationUpdater.locationUpdateCallback(x.lat, x.lng)
    }

    @Test
    fun should_not_read_return_instructions_before_beginning_ones_even_if_location_is_the_same() {
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)
        val handleAnnouncement = { announcement: String ->
            println(announcement)
//            assertEquals("Announcement should be 21","21", announcement)
            1
        }
        val locationUpdater = setupAssertion(handleAnnouncement, createInstructions(steps))

        val initialInstruction = steps[22]
        val location = initialInstruction.last()
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
        locationUpdater.locationUpdateCallback(location.lat, location.lng)
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

    private fun getInstructionsWithOverlaps(): List<Instruction> {
        val steps = MockData.getDecodedPolyline(MockData.stepsWithOverlap)
        return createInstructions(steps)
    }

    private fun createInstructions(steps: List<List<LatLng>>): List<Instruction> {
        return steps.mapIndexed { index, steps -> Instruction(steps, index.toString()) }
    }
}
