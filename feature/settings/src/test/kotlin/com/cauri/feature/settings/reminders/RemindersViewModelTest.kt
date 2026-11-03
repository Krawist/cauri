package com.cauri.feature.settings.reminders

import com.cauri.core.capture.reminder.FakeReminderLearner
import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.PlaceSpendRate
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeDecisionLogRepository
import com.cauri.core.data.repository.FakePlaceRepository
import com.cauri.core.engine.SummaryTimeImpl
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
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
    private val log = FakeDecisionLogRepository()
    private val learner = FakeReminderLearner()
    private lateinit var viewModel: RemindersViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = viewModel()
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
    inner class OnSummaryTime {
        @Test
        fun `should show the time of the summary the user chose`() {
            // when
            viewModel.onSummaryTime(LocalTime.of(21, 30))

            // then
            preferences.reminders.value.summaryTime shouldBe LocalTime.of(21, 30)
            viewModel.uiState.value.summaryTime shouldBe LocalTime.of(21, 30)
        }

        @Test
        fun `should show 20 30 while the summary follows answers not known yet`() {
            // when
            viewModel.onSummaryTime(null)

            // then
            viewModel.uiState.value.summaryTime shouldBe LocalTime.of(20, 30)
        }
    }

    @Nested
    inner class Lessons {
        @Test
        fun `should tell the shops where a visit leads to an expense often, and rarely`() {
            // given
            log.rates = listOf(
                PlaceSpendRate(name = "Marché Mokolo", visits = 10, withExpense = 9),
                PlaceSpendRate(name = "Station Total", visits = 8, withExpense = 1),
                PlaceSpendRate(name = "Boulangerie", visits = 3, withExpense = 3),
            )

            // when
            val viewModel = viewModel()

            // then
            viewModel.uiState.value.lessons shouldContainExactly listOf(
                Lesson.OftenSpends(place = "Marché Mokolo", timesInTen = 9),
                Lesson.RarelySpends(place = "Station Total"),
            )
        }

        @Test
        fun `should tell the two hours the user answers most in`() {
            // given
            log.answers = List(6) { Instant.parse("2026-11-01T19:10:00Z") } +
                List(5) { Instant.parse("2026-11-02T20:40:00Z") } + Instant.parse("2026-11-02T08:00:00Z")

            // when
            val viewModel = viewModel()

            // then
            viewModel.uiState.value.lessons shouldContainExactly listOf(Lesson.AnswerHours(from = 19, until = 21))
        }
    }

    @Nested
    inner class Weeks {
        @Test
        fun `should count the weeks from the first answer`() {
            // given
            log.answers = listOf(Instant.parse("2026-10-19T19:00:00Z"), Instant.parse("2026-11-02T19:00:00Z"))

            // when
            val viewModel = viewModel()

            // then
            viewModel.uiState.value.weeks shouldBe 2
        }
    }

    @Nested
    inner class OnReset {
        @Test
        fun `should forget what was learnt`() {
            // given
            viewModel.onAskReset(open = true)

            // when
            viewModel.onReset()

            // then
            learner.resets shouldContainExactly listOf(Instant.parse("2026-11-03T12:00:00Z"))
            viewModel.uiState.value.confirmingReset.shouldBeFalse()
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

    private fun viewModel() = RemindersViewModel(
        preferences = preferences,
        places = places,
        log = log,
        summaryTime = SummaryTimeImpl(),
        learner = learner,
        clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC),
    )

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
