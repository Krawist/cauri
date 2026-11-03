package com.cauri.core.capture.place

import com.cauri.core.data.repository.VisitRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [VisitRecorder]. */
@Singleton
internal class VisitRecorderImpl @Inject constructor(private val visits: VisitRepository) : VisitRecorder {
    override suspend fun record(transition: PlaceTransition, placeIds: List<Long>, at: Instant) = placeIds.forEach {
        when (transition) {
            PlaceTransition.STAYED -> visits.enter(placeId = it, at = at - VisitRecorder.STAY)
            PlaceTransition.LEFT -> visits.leave(placeId = it, at = at)
        }
    }
}
