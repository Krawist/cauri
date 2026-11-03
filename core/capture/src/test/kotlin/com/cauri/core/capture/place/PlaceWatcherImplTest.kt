package com.cauri.core.capture.place

import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.FakePlaceRepository
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlaceWatcherImplTest {
    private val places = FakePlaceRepository()
    private val geofences = RecordingGeofenceService()
    private val watcher = PlaceWatcherImpl(places, geofences, UnconfinedTestDispatcher())

    @Nested
    inner class Start {
        @Test
        fun `should watch the places, and again after each change`() = runTest {
            // given
            watcher.start()

            // when
            places.addPlace(place(name = "Marché Mokolo"))

            // then
            geofences.watched.map { list -> list.map { it.name } } shouldContainExactly
                listOf(emptyList(), listOf("Marché Mokolo"))
        }

        @Test
        fun `should watch once when started twice`() = runTest {
            // when
            watcher.start()
            watcher.start()

            // then
            geofences.watched shouldContainExactly listOf(emptyList())
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should watch the same places again`() = runTest {
            // given
            places.addPlace(place(name = "Marché Mokolo"))
            watcher.start()

            // when
            watcher.refresh()

            // then
            geofences.watched.map { list -> list.map { it.name } } shouldContainExactly
                listOf(listOf("Marché Mokolo"), listOf("Marché Mokolo"))
        }
    }

    private fun place(name: String) = NewPlace(
        name = name,
        latitude = 3.88,
        longitude = 11.50,
        radiusMeters = 80,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
        remindAfterVisit = true,
        area = null,
        address = null,
    )

    private class RecordingGeofenceService : GeofenceService {
        val watched = mutableListOf<List<Place>>()

        override suspend fun watch(places: List<Place>): Boolean {
            watched += places
            return true
        }
    }
}
