package com.cauri.core.capture.place

import com.cauri.core.capture.reminder.CountingDecider
import com.cauri.core.data.repository.FakeVisitRepository
import com.cauri.core.data.repository.FakeVisitRepository.Move
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VisitRecorderImplTest {
    private val visits = FakeVisitRepository()
    private val reminders = CountingDecider()
    private val recorder = VisitRecorderImpl(visits, reminders)

    @Nested
    inner class Record {
        @Test
        fun `should open a visit from when the user came in, five minutes before the stay`() = runTest {
            // when
            recorder.record(transition = PlaceTransition.STAYED, placeIds = listOf(3), at = NOON)

            // then
            visits.moves shouldContainExactly listOf(Move(placeId = 3, at = NOON.minusSeconds(300), entered = true))
        }

        @Test
        fun `should close the visit when the user leaves`() = runTest {
            // when
            recorder.record(transition = PlaceTransition.LEFT, placeIds = listOf(3), at = NOON)

            // then
            visits.moves shouldContainExactly listOf(Move(placeId = 3, at = NOON, entered = false))
        }

        @Test
        fun `should decide about the card of a shop once left`() = runTest {
            // when
            recorder.record(transition = PlaceTransition.LEFT, placeIds = listOf(3), at = NOON)

            // then
            reminders.runs shouldBe 1
        }

        @Test
        fun `should decide nothing when the user only stays`() = runTest {
            // when
            recorder.record(transition = PlaceTransition.STAYED, placeIds = listOf(3), at = NOON)

            // then
            reminders.runs shouldBe 0
        }

        @Test
        fun `should record the transition at each place told`() = runTest {
            // when
            recorder.record(transition = PlaceTransition.LEFT, placeIds = listOf(3, 4), at = NOON)

            // then
            visits.moves.map { it.placeId } shouldContainExactly listOf(3L, 4L)
        }
    }

    private companion object {
        val NOON: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
