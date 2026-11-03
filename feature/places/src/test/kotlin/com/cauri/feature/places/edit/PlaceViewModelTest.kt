package com.cauri.feature.places.edit

import androidx.lifecycle.SavedStateHandle
import com.cauri.core.capture.place.FakeAddressService
import com.cauri.core.capture.place.FakeLocationService
import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.capture.place.Position
import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.FakeCategoryRepository
import com.cauri.core.data.repository.FakePlaceRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
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
internal class PlaceViewModelTest {
    private val categories = FakeCategoryRepository()
    private val places = FakePlaceRepository()
    private val location = FakeLocationService()
    private val addresses = FakeAddressService()
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnLocate {
        @Test
        fun `should keep where the phone stands, with its accuracy`() {
            // given
            location.position = Position(latitude = 3.88, longitude = 11.5, accuracyMeters = 12)
            val viewModel = newPlace()

            // when
            viewModel.onLocate()

            // then
            viewModel.uiState.value.let { listOf(it.latitude, it.longitude, it.accuracyMeters) } shouldBe
                listOf(3.88, 11.5, 12)
        }

        @Test
        fun `should show the address of the position, and name the place as the map does`() {
            // given
            location.position = Position(latitude = 3.88, longitude = 11.5, accuracyMeters = 12)
            addresses.address = bastos()
            val viewModel = newPlace()

            // when
            viewModel.onLocate()

            // then
            viewModel.uiState.value.let { listOf(it.query, it.address, it.area, it.name) } shouldBe
                listOf(
                    "Carrefour Bastos, Bastos, Yaoundé",
                    "Carrefour Bastos, Bastos, Yaoundé",
                    "Bastos",
                    "Carrefour Bastos",
                )
        }

        @Test
        fun `should say the position was not found`() {
            // given
            val viewModel = newPlace()

            // when
            viewModel.onLocate()

            // then
            viewModel.uiState.value.notFound.shouldBeTrue()
        }
    }

    @Nested
    inner class OnQuery {
        @Test
        fun `should find the addresses once the user stops typing`() {
            // given
            addresses.found["Bastos"] = listOf(bastos())
            val viewModel = newPlace()

            // when
            viewModel.onQuery("Bastos")
            dispatcher.scheduler.advanceUntilIdle()

            // then
            viewModel.uiState.value.results shouldContainExactly listOf(bastos())
        }

        @Test
        fun `should not look for fewer than three letters`() {
            // given
            addresses.found["Ba"] = listOf(bastos())
            val viewModel = newPlace()

            // when
            viewModel.onQuery("Ba")
            dispatcher.scheduler.advanceUntilIdle()

            // then
            viewModel.uiState.value.let { it.results.isEmpty() && !it.searched }.shouldBeTrue()
        }
    }

    @Nested
    inner class OnChooseAddress {
        @Test
        fun `should put the place at the address, shown in the field and naming it`() {
            // given
            val viewModel = newPlace()

            // when
            viewModel.onChooseAddress(bastos())

            // then
            viewModel.uiState.value.let { listOf(it.latitude, it.name, it.area, it.query) } shouldBe
                listOf(3.89, "Carrefour Bastos", "Bastos", "Carrefour Bastos, Bastos, Yaoundé")
        }

        @Test
        fun `should leave the name empty when the map knows only a street`() {
            // given
            val viewModel = newPlace()

            // when
            viewModel.onChooseAddress(bastos().copy(name = null))

            // then
            viewModel.uiState.value.name shouldBe ""
        }

        @Test
        fun `should keep the name typed before`() {
            // given
            val viewModel = newPlace()
            viewModel.onName("Chez maman")

            // when
            viewModel.onChooseAddress(bastos())

            // then
            viewModel.uiState.value.name shouldBe "Chez maman"
        }
    }

    @Nested
    inner class OnSave {
        @Test
        fun `should not save without a position`() = runTest {
            // given
            val viewModel = newPlace()
            viewModel.onName("Marché Mokolo")

            // when
            viewModel.onSave()

            // then
            places.places.value.shouldBeEmpty()
            viewModel.uiState.value.closed.shouldBeFalse()
        }

        @Test
        fun `should add the place where the phone stands`() = runTest {
            // given
            location.position = Position(latitude = 3.88, longitude = 11.5, accuracyMeters = 12)
            val viewModel = newPlace()
            viewModel.onLocate()
            viewModel.onName("Marché Mokolo")
            viewModel.onCategory(10)
            viewModel.onRadius(120)

            // when
            viewModel.onSave()

            // then
            places.places.value.map { listOf(it.name, it.latitude, it.radiusMeters, it.defaultCategoryId) } shouldBe
                listOf(listOf("Marché Mokolo", 3.88, 120, 10L))
            viewModel.uiState.value.closed.shouldBeTrue()
        }

        @Test
        fun `should give home no default category`() = runTest {
            // given
            location.position = Position(latitude = 3.88, longitude = 11.5, accuracyMeters = 12)
            val viewModel = newPlace()
            viewModel.onLocate()
            viewModel.onName("Maison")
            viewModel.onCategory(10)
            viewModel.onKind(PlaceKind.HOME)

            // when
            viewModel.onSave()

            // then
            places.places.value.map { it.defaultCategoryId } shouldContainExactly listOf(null)
        }

        @Test
        fun `should save the address chosen`() = runTest {
            // given
            val viewModel = newPlace()
            viewModel.onChooseAddress(bastos())

            // when
            viewModel.onSave()

            // then
            places.places.value.map { it.address to it.area } shouldContainExactly
                listOf("Carrefour Bastos, Bastos, Yaoundé" to "Bastos")
        }

        @Test
        fun `should change a place saved before, keeping its position`() = runTest {
            // given
            val id = places.addPlace(place(name = "Marché Mokolo"))
            val viewModel =
                PlaceViewModel(SavedStateHandle(mapOf("placeId" to id)), categories, places, location, addresses)
            viewModel.onName("Marché Central")

            // when
            viewModel.onSave()

            // then
            places.places.value.map { it.name to it.latitude } shouldContainExactly listOf("Marché Central" to 3.88)
        }
    }

    @Nested
    inner class OnRadius {
        @Test
        fun `should keep the radius within its limits`() {
            // given
            val viewModel = newPlace()

            // when
            viewModel.onRadius(1_000)

            // then
            viewModel.uiState.value.radiusMeters shouldBe 300
        }
    }

    @Nested
    inner class OnDelete {
        @Test
        fun `should delete the place and close`() = runTest {
            // given
            val id = places.addPlace(place(name = "Marché Mokolo"))
            val viewModel =
                PlaceViewModel(SavedStateHandle(mapOf("placeId" to id)), categories, places, location, addresses)

            // when
            viewModel.onDelete()

            // then
            places.places.value.shouldBeEmpty()
            viewModel.uiState.value.closed.shouldBeTrue()
        }
    }

    private fun newPlace() = PlaceViewModel(SavedStateHandle(), categories, places, location, addresses)

    private fun bastos() = FoundAddress(
        label = "Carrefour Bastos, Bastos, Yaoundé",
        name = "Carrefour Bastos",
        area = "Bastos",
        latitude = 3.89,
        longitude = 11.51,
    )

    private fun place(name: String) = NewPlace(
        name = name,
        latitude = 3.88,
        longitude = 11.5,
        radiusMeters = 80,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
        remindAfterVisit = true,
        area = null,
        address = null,
    )
}
