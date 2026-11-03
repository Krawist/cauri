package com.cauri.core.data.repository

import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.Place
import kotlinx.coroutines.flow.Flow

/** The places the user declared: shops to be reminded about, home and work. */
interface PlaceRepository {
    /** Every place by name, with its visits, again after each change. */
    val places: Flow<List<Place>>

    /** The places to watch with a geofence, again after each change: at most 100, the most visited first. */
    val watched: Flow<List<Place>>

    /** The place [id]; `null` when it does not exist. */
    suspend fun getPlace(id: Long): Place?

    /** Saves [place] and returns its id. */
    suspend fun addPlace(place: NewPlace): Long

    /** Replaces the values of the place [id] with [place]; its visits stay. */
    suspend fun updatePlace(id: Long, place: NewPlace)

    /** Deletes the place [id] and its visits; the transactions made there lose only their place. */
    suspend fun deletePlace(id: Long)

    companion object {
        /** Smallest radius of a place, in metres: below it a geofence misses many visits. */
        const val MIN_RADIUS_METERS = 50

        /** Largest radius of a place, in metres: a whole market, not a neighbourhood. */
        const val MAX_RADIUS_METERS = 300

        /** Radius of a new place, in metres: a shop and its doorstep. */
        const val DEFAULT_RADIUS_METERS = 80
    }
}
