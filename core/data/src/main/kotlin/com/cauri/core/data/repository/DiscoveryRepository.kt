package com.cauri.core.data.repository

import com.cauri.core.data.model.PlaceSuggestion
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

/**
 * The places the user often stops at, found from the positions of the phone, that Cauri asks about once. Positions
 * are kept 30 days at most, and only where the phone stayed.
 */
interface DiscoveryRepository {
    /** The suggestions not answered yet, the oldest first, again after each change. */
    val pending: Flow<List<PlaceSuggestion>>

    /** Keeps the position at [latitude] and [longitude] read at [at]: a stop starts or goes on. */
    suspend fun recordPosition(latitude: Double, longitude: Double, at: Instant)

    /**
     * Looks for the places to suggest at [at], days counted in [zone], saves them and returns them; forgets the
     * positions alone, that no other joined.
     */
    suspend fun findSuggestions(at: Instant, zone: ZoneId): List<PlaceSuggestion>

    /** The suggestion [id]; `null` when it does not exist. */
    suspend fun getSuggestion(id: Long): PlaceSuggestion?

    /** The suggestion not answered yet that the notification asked about last; `null` when there is none. */
    suspend fun getLastNotified(): PlaceSuggestion?

    /** Keeps [area] as the neighbourhood of the suggestion [id]. */
    suspend fun setArea(id: Long, area: String?)

    /** Remembers that a notification asked about the suggestion [id] at [at]. */
    suspend fun markNotified(id: Long, at: Instant)

    /** « Oui » when [accepted], the place saved; « Non » otherwise, the place ignored for good. */
    suspend fun answer(id: Long, accepted: Boolean)
}
