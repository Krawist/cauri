package com.cauri.core.data.repository

import com.cauri.core.data.model.PlaceSuggestion
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [DiscoveryRepository] for tests: it keeps the positions recorded and the answers, and finds the
 * suggestions of [found].
 */
class FakeDiscoveryRepository : DiscoveryRepository {
    override val pending = MutableStateFlow<List<PlaceSuggestion>>(emptyList())

    /** Suggestions [findSuggestions] answers, once. */
    val found = mutableListOf<PlaceSuggestion>()

    val positions = mutableListOf<Pair<Double, Double>>()
    val areas = mutableMapOf<Long, String?>()
    val notified = mutableListOf<Long>()

    /** Answers given, by suggestion: `true` for « Oui ». */
    val answers = mutableMapOf<Long, Boolean>()

    override suspend fun recordPosition(latitude: Double, longitude: Double, at: Instant) {
        positions += latitude to longitude
    }

    override suspend fun findSuggestions(at: Instant, zone: ZoneId): List<PlaceSuggestion> =
        found.toList().also { found.clear() }

    override suspend fun getSuggestion(id: Long) = pending.value.firstOrNull { it.id == id }

    override suspend fun getLastNotified() = pending.value.lastOrNull { it.notified }

    override suspend fun setArea(id: Long, area: String?) {
        areas[id] = area
    }

    override suspend fun markNotified(id: Long, at: Instant) {
        notified += id
    }

    override suspend fun answer(id: Long, accepted: Boolean) {
        answers[id] = accepted
        pending.value = pending.value.filterNot { it.id == id }
    }
}
