package com.cauri.core.data.repository

import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.Place
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PlaceRepository] for tests: it keeps the places saved, in the order added, and watches them all. */
class FakePlaceRepository : PlaceRepository {
    override val places = MutableStateFlow<List<Place>>(emptyList())

    override val watched = places

    override suspend fun getPlace(id: Long) = places.value.firstOrNull { it.id == id }

    override suspend fun addPlace(place: NewPlace): Long {
        val id = (places.value.maxOfOrNull { it.id } ?: 0) + 1
        places.value += place.toPlace(id = id, visits = 0)
        return id
    }

    override suspend fun updatePlace(id: Long, place: NewPlace) {
        places.value = places.value.map { if (it.id == id) place.toPlace(id = id, visits = it.visits) else it }
    }

    override suspend fun deletePlace(id: Long) {
        places.value = places.value.filterNot { it.id == id }
    }

    private fun NewPlace.toPlace(id: Long, visits: Int) = Place(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters,
        kind = kind,
        defaultCategoryId = defaultCategoryId,
        remindAfterVisit = remindAfterVisit,
        area = area,
        address = address,
        visits = visits,
    )
}
