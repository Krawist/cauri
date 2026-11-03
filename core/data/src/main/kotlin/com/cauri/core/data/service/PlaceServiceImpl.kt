package com.cauri.core.data.service

import com.cauri.core.data.model.Place
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [PlaceService]. */
@Singleton
internal class PlaceServiceImpl @Inject constructor() : PlaceService {
    override fun watched(places: List<Place>): List<Place> =
        places.sortedWith(compareByDescending<Place> { it.visits }.thenByDescending { it.id }).take(MAX_GEOFENCES)

    private companion object {
        // Android refuses more active geofences for one app.
        const val MAX_GEOFENCES = 100
    }
}
