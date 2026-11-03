package com.cauri.core.data.service

import com.cauri.core.data.model.Place

/** The rules of the places the user declared. */
internal interface PlaceService {
    /**
     * The places of [places] to watch with a geofence: all of them up to 100, the limit of Android; beyond it the most
     * visited, then the newest, win.
     */
    fun watched(places: List<Place>): List<Place>
}
