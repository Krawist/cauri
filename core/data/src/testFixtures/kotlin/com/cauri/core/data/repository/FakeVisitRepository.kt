package com.cauri.core.data.repository

import java.time.Instant

/** In-memory [VisitRepository] for tests: it keeps the entries and departures in the order told. */
class FakeVisitRepository : VisitRepository {
    /**
     * An entry or a departure told to the repository.
     *
     * @property placeId Place entered or left.
     * @property at When.
     * @property entered Whether the user entered, rather than left.
     */
    data class Move(val placeId: Long, val at: Instant, val entered: Boolean)

    val moves = mutableListOf<Move>()

    override suspend fun enter(placeId: Long, at: Instant) {
        moves += Move(placeId = placeId, at = at, entered = true)
    }

    override suspend fun leave(placeId: Long, at: Instant) {
        moves += Move(placeId = placeId, at = at, entered = false)
    }
}
