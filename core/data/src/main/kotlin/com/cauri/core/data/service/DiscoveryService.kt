package com.cauri.core.data.service

import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.PlaceSuggestionEntity
import com.cauri.core.data.database.entity.StopEntity
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** The rules that find, from the stops of the phone, the places the user often stops at. */
internal interface DiscoveryService {
    /**
     * Whether the position at [latitude] and [longitude] read at [at] keeps the phone at [stop]: within 150 m of it,
     * from its last position to [GAP] after it.
     */
    fun continues(stop: StopEntity, latitude: Double, longitude: Double, at: Instant): Boolean

    /**
     * The places to suggest at [at], from [stops]: stops within 150 m of each other, on 3 different days of the last
     * 30 at least, none of them at night (22 h to 5 h in [zone]: where the user sleeps is home, not a shop). A place
     * near one of [places] or of the [known] suggestions, answered or not, is never suggested again.
     */
    fun suggestions(
        stops: List<StopEntity>,
        places: List<PlaceEntity>,
        known: List<PlaceSuggestionEntity>,
        zone: ZoneId,
        at: Instant,
    ): List<PlaceSuggestionEntity>

    companion object {
        /** How long positions are kept, and the stops counted. */
        val WINDOW: Duration = Duration.ofDays(30)

        /** Longest time between two positions of a stop: read every quarter of an hour, Android may delay one. */
        val GAP: Duration = Duration.ofMinutes(40)
    }
}
