package com.cauri.core.data.repository

import com.cauri.core.data.model.PendingVisit
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * The stays of the user at the places they declared, noticed by their geofences. Leaving a shop with nothing paid
 * there creates a cash expense to confirm, its card in « À confirmer ».
 */
interface VisitRepository {
    /** The visits whose card still waits, left after the last cash count, the latest first, again after changes. */
    val waiting: Flow<List<PendingVisit>>

    /** The user has stayed at the place [placeId] since [at]; a stay already open there goes on. */
    suspend fun enter(placeId: Long, at: Instant)

    /**
     * The user left the place [placeId] at [at]; nothing when they were not known to be there. A shop left with
     * nothing paid during the visit gets its card: a cash expense to confirm, at 0 until the user types the amount,
     * proposing the usual one there.
     */
    suspend fun leave(placeId: Long, at: Instant)

    /**
     * Gives the place of a visit to the payments without place made during it, read in a message or typed: they are
     * what was spent there, and the card of the visit goes. Looks at the visits of the day before [at].
     */
    suspend fun attachTransactions(at: Instant)

    /**
     * Settles the cards of the visits at a cash count at [at]: those of [amounts], by transaction, are confirmed with
     * their amount (minor units); the others from before [at] are set aside, the count holding them.
     */
    suspend fun settle(amounts: Map<Long, Long>, at: Instant)
}
