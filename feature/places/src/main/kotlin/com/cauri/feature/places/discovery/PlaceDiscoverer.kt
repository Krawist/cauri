package com.cauri.feature.places.discovery

import java.time.Instant

/** Finds, from the positions of the phone, the places the user often stops at, and asks about each once. */
internal interface PlaceDiscoverer {
    /**
     * Reads the position of the phone, by Wi-Fi and cell towers, keeps it and asks about a place newly found; nothing
     * without the location « always ».
     */
    suspend fun discover()

    /** Keeps the position at [latitude] and [longitude] read at [at]. */
    suspend fun record(latitude: Double, longitude: Double, at: Instant)

    /** Looks at [at] for places to suggest, and asks about the first one found with a notification. */
    suspend fun ask(at: Instant)
}
