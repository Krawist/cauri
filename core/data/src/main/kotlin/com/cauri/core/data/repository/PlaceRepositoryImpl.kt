package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.PlaceDao
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.service.PlaceService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [PlaceRepository]. */
@Singleton
internal class PlaceRepositoryImpl @Inject constructor(
    private val placeDao: PlaceDao,
    private val placeService: PlaceService,
) : PlaceRepository {
    override val places: Flow<List<Place>> = placeDao.observeWithVisits().map { places ->
        places.map { it.place.toPlace(visits = it.visits) }
    }

    override val watched: Flow<List<Place>> = places.map(placeService::watched)

    override suspend fun getPlace(id: Long): Place? = placeDao.getById(id)?.toPlace(visits = 0)

    override suspend fun addPlace(place: NewPlace): Long = placeDao.insert(place.toEntity(id = 0))

    override suspend fun updatePlace(id: Long, place: NewPlace) = placeDao.update(place.toEntity(id = id))

    override suspend fun deletePlace(id: Long) = placeDao.delete(id)

    private fun PlaceEntity.toPlace(visits: Int) = Place(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters.toInt(),
        kind = kind,
        defaultCategoryId = defaultCategoryId,
        remindAfterVisit = remindAfterVisit,
        area = area,
        address = address,
        visits = visits,
    )

    private fun NewPlace.toEntity(id: Long) = PlaceEntity(
        id = id,
        name = name.trim(),
        latitude = latitude,
        longitude = longitude,
        radiusMeters = radiusMeters.toFloat(),
        kind = kind,
        defaultCategoryId = defaultCategoryId,
        // Home and work never bring a reminder.
        remindAfterVisit = remindAfterVisit && kind == PlaceKind.SHOP,
        area = area,
        address = address,
    )
}
