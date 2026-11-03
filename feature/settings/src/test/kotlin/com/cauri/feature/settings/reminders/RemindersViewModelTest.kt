package com.cauri.feature.settings.reminders

import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakePlaceRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class RemindersViewModelTest {
    private val preferences = FakeUserPreferencesRepository()
    private val places = FakePlaceRepository()
    private lateinit var viewModel: RemindersViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RemindersViewModel(preferences = preferences, places = places)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should show the limits the user saved`() {
            // given
            val saved = ReminderSettings(quietFrom = 23, quietUntil = 6, dailyCap = 2, visitReminders = false)

            // when
            preferences.reminders.value = saved

            // then
            viewModel.uiState.value.settings shouldBe saved
        }

        @Test
        fun `should list home and work, then the shops without reminder`() {
            // when
            places.places.value = listOf(
                place(id = 1, name = "Boulangerie Saker", kind = PlaceKind.SHOP, remind = false),
                place(id = 2, name = "Marché Mokolo", kind = PlaceKind.SHOP, remind = true),
                place(id = 3, name = "Maison", kind = PlaceKind.HOME, remind = false),
            )

            // then
            viewModel.uiState.value.quietPlaces shouldContainExactly listOf("Maison", "Boulangerie Saker")
        }
    }

    @Nested
    inner class OnVisitReminders {
        @Test
        fun `should save the reminders after visits turned off`() {
            // when
            viewModel.onVisitReminders(on = false)

            // then
            preferences.reminders.value.visitReminders.shouldBeFalse()
        }
    }

    @Nested
    inner class OnQuietHours {
        @Test
        fun `should open the sheet of the quiet hours`() {
            // when
            viewModel.onQuietHours(open = true)

            // then
            viewModel.uiState.value.choosingQuietHours.shouldBeTrue()
        }
    }

    @Nested
    inner class OnQuietFrom {
        @ParameterizedTest
        @CsvSource("22, 1, 23", "23, 1, 0", "0, -1, 23")
        fun `should move the start of the quiet hours round the clock`(from: Int, step: Int, expected: Int) {
            // given
            preferences.reminders.value = preferences.reminders.value.copy(quietFrom = from)

            // when
            viewModel.onQuietFrom(step)

            // then
            preferences.reminders.value.quietFrom shouldBe expected
        }
    }

    @Nested
    inner class OnQuietUntil {
        @Test
        fun `should move the end of the quiet hours`() {
            // when
            viewModel.onQuietUntil(-1)

            // then
            preferences.reminders.value.quietUntil shouldBe 6
        }
    }

    @Nested
    inner class OnDailyCap {
        @ParameterizedTest
        @CsvSource("4, 1, 5", "4, -1, 3", "1, -1, 1", "10, 1, 10")
        fun `should change the daily cap within its bounds`(cap: Int, step: Int, expected: Int) {
            // given
            preferences.reminders.value = preferences.reminders.value.copy(dailyCap = cap)

            // when
            viewModel.onDailyCap(step)

            // then
            preferences.reminders.value.dailyCap shouldBe expected
        }
    }

    private fun place(id: Long, name: String, kind: PlaceKind, remind: Boolean) = Place(
        id = id,
        name = name,
        latitude = 4.05,
        longitude = 9.77,
        radiusMeters = 80,
        kind = kind,
        defaultCategoryId = null,
        remindAfterVisit = remind,
        area = null,
        address = null,
        visits = 0,
    )
}
