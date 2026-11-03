package com.cauri.core.data.repository

import java.time.Instant

/** The stays of the user at the places they declared, noticed by their geofences. */
interface VisitRepository {
    /** The user has stayed at the place [placeId] since [at]; a stay already open there goes on. */
    suspend fun enter(placeId: Long, at: Instant)

    /** The user left the place [placeId] at [at]; nothing when they were not known to be there. */
    suspend fun leave(placeId: Long, at: Instant)
}
