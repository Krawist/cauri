package com.cauri.core.capture.place

import java.time.Duration
import java.time.Instant

/** Turns what Android tells about the places into visits. */
interface VisitRecorder {
    /**
     * Records [transition] at the places [placeIds], told at [at]: a stay opens a visit from when the user came in,
     * five minutes before; a departure closes it.
     */
    suspend fun record(transition: PlaceTransition, placeIds: List<Long>, at: Instant)

    companion object {
        /** How long the user stays in a place before it counts as a visit: passing by on a moto never does. */
        val STAY: Duration = Duration.ofMinutes(5)
    }
}
