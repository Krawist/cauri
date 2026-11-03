package com.cauri.core.capture.place

import com.cauri.core.data.model.Place

/** The geofences Android watches for Cauri, one per place, to notice the visits with the app closed. */
interface GeofenceService {
    /**
     * Replaces Cauri's geofences with one per place of [places], and returns whether Android watches them: never
     * without the location in the background, nor on a phone without Google Play services.
     */
    suspend fun watch(places: List<Place>): Boolean
}
