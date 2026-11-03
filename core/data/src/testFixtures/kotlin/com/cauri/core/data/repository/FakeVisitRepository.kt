package com.cauri.core.data.repository

import com.cauri.core.data.model.PendingVisit
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

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

    override val waiting = MutableStateFlow<List<PendingVisit>>(emptyList())

    /** How many times [attachTransactions] ran. */
    var attachRuns = 0

    /** The amounts confirmed by [settle], by card, one map per call. */
    val settled = mutableListOf<Map<Long, Long>>()

    override suspend fun enter(placeId: Long, at: Instant) {
        moves += Move(placeId = placeId, at = at, entered = true)
    }

    override suspend fun leave(placeId: Long, at: Instant) {
        moves += Move(placeId = placeId, at = at, entered = false)
    }

    override suspend fun attachTransactions(at: Instant) {
        attachRuns++
    }

    override suspend fun settle(amounts: Map<Long, Long>, at: Instant) {
        settled += amounts
    }
}
