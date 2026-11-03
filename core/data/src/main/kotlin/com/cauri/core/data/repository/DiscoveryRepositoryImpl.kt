package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.DiscoveryDao
import com.cauri.core.data.database.dao.PlaceDao
import com.cauri.core.data.database.entity.PlaceSuggestionEntity
import com.cauri.core.data.database.entity.StopEntity
import com.cauri.core.data.database.entity.SuggestionAnswer
import com.cauri.core.data.model.PlaceSuggestion
import com.cauri.core.data.service.DiscoveryService
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Default implementation for [DiscoveryRepository]. */
@Singleton
internal class DiscoveryRepositoryImpl @Inject constructor(
    private val discoveryDao: DiscoveryDao,
    private val placeDao: PlaceDao,
    private val discovery: DiscoveryService,
) : DiscoveryRepository {
    override val pending: Flow<List<PlaceSuggestion>> =
        discoveryDao.observePending().map { suggestions -> suggestions.map { it.toSuggestion() } }

    override suspend fun recordPosition(latitude: Double, longitude: Double, at: Instant) {
        // The stop the position belongs to, among the latest ones: positions may come late or out of order.
        val stop = discoveryDao.getEndedBetween(from = at - DiscoveryService.GAP, to = at)
            .firstOrNull { discovery.continues(it, latitude = latitude, longitude = longitude, at = at) }
        discoveryDao.recordPosition(
            extended = stop?.let { it.copy(endedAt = at, positions = it.positions + 1) },
            started = if (stop != null) {
                null
            } else {
                StopEntity(latitude = latitude, longitude = longitude, startedAt = at, endedAt = at, positions = 1)
            },
            before = at - DiscoveryService.WINDOW,
        )
    }

    override suspend fun findSuggestions(at: Instant, zone: ZoneId): List<PlaceSuggestion> {
        // A position no other joined since is not a stop: only the stops are kept.
        discoveryDao.deleteAloneBefore(at - DiscoveryService.GAP)
        val found = discovery.suggestions(
            stops = discoveryDao.getStopsSince(at - DiscoveryService.WINDOW),
            places = placeDao.observeAll().first(),
            known = discoveryDao.getSuggestions(),
            zone = zone,
            at = at,
        )
        if (found.isEmpty()) return emptyList()
        val ids = discoveryDao.insertSuggestions(found)
        return found.zip(ids) { suggestion, id -> suggestion.copy(id = id).toSuggestion() }
    }

    override suspend fun getSuggestion(id: Long): PlaceSuggestion? = discoveryDao.getSuggestion(id)?.toSuggestion()

    override suspend fun getLastNotified(): PlaceSuggestion? = discoveryDao.getLastNotified()?.toSuggestion()

    override suspend fun setArea(id: Long, area: String?) = discoveryDao.setArea(id = id, area = area)

    override suspend fun markNotified(id: Long, at: Instant) = discoveryDao.setNotified(id = id, at = at)

    override suspend fun answer(id: Long, accepted: Boolean) = discoveryDao.setAnswer(
        id = id,
        answer = if (accepted) SuggestionAnswer.ACCEPTED else SuggestionAnswer.REFUSED,
    )

    private fun PlaceSuggestionEntity.toSuggestion() = PlaceSuggestion(
        id = id,
        latitude = latitude,
        longitude = longitude,
        area = area,
        stops = stops,
        notified = notifiedAt != null,
    )
}
