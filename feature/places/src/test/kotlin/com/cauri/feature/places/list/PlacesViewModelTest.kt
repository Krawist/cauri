package com.cauri.feature.places.list

import com.cauri.core.capture.permission.FakePermissionService
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.place.FakePlaceWatcher
import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakePlaceRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlacesViewModelTest {
    private val places = FakePlaceRepository()
    private val categories = FakeCategoryRepository()
    private val permissions = FakePermissionService()
    private val watcher = FakePlaceWatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should list the shops apart from home and work`() = runTest {
            // given
            places.addPlace(place(name = "Marché Mokolo", kind = PlaceKind.SHOP))
            places.addPlace(place(name = "Maison", kind = PlaceKind.HOME))

            // when
            val viewModel = PlacesViewModel(places, categories, permissions, watcher)

            // then
            viewModel.uiState.value.let { state -> state.shops.map { it.name } to state.quiet.map { it.name } } shouldBe
                (listOf("Marché Mokolo") to listOf("Maison"))
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should register the geofences again once the location is allowed`() {
            // given
            val viewModel = PlacesViewModel(places, categories, permissions, watcher)
            permissions.status = permissions.status.copy(location = LocationAccess.ALWAYS)

            // when
            viewModel.refresh()

            // then
            listOf(viewModel.uiState.value.location, watcher.refreshes) shouldContainExactly
                listOf(LocationAccess.ALWAYS, 1)
        }

        @Test
        fun `should not register them again when nothing changed`() {
            // given
            val viewModel = PlacesViewModel(places, categories, permissions, watcher)

            // when
            viewModel.refresh()

            // then
            watcher.refreshes shouldBe 0
        }
    }

    private fun place(name: String, kind: PlaceKind) = NewPlace(
        name = name,
        latitude = 3.88,
        longitude = 11.5,
        radiusMeters = 80,
        kind = kind,
        defaultCategoryId = null,
        remindAfterVisit = true,
        area = null,
        address = null,
    )
}
