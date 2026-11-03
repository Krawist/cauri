package com.cauri.core.capture.place

import com.cauri.core.capture.reminder.ReminderDecider
import com.cauri.core.data.repository.VisitRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [VisitRecorder]. */
@Singleton
internal class VisitRecorderImpl @Inject constructor(
    private val visits: VisitRepository,
    private val reminders: ReminderDecider,
) : VisitRecorder {
    override suspend fun record(transition: PlaceTransition, placeIds: List<Long>, at: Instant) {
        placeIds.forEach {
            when (transition) {
                PlaceTransition.STAYED -> visits.enter(placeId = it, at = at - VisitRecorder.STAY)
                PlaceTransition.LEFT -> visits.leave(placeId = it, at = at)
            }
        }
        // A shop left with nothing paid has its card: notify now, in the evening, or not at all.
        if (transition == PlaceTransition.LEFT) reminders.decidePending()
    }
}
