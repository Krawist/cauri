package com.cauri.feature.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.reminder.ReminderLearner
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.PlaceSpendRate
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.DecisionLogRepository
import com.cauri.core.data.repository.PlaceRepository
import com.cauri.core.engine.SummaryTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « Rappels intelligents »: the guardrails the user sets on the reminders (after visits or not, the quiet hours, the
 * daily cap), the time of the evening summary and the places that never bring one. Each change is saved at once.
 */
// One function per gesture of the screen, and the lessons told.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class RemindersViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    places: PlaceRepository,
    private val log: DecisionLogRepository,
    private val summaryTime: SummaryTime,
    private val learner: ReminderLearner,
    private val clock: Clock,
) : ViewModel() {
    private val state = MutableStateFlow(RemindersUiState())

    val uiState: StateFlow<RemindersUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.reminders.collect { settings ->
                state.update { it.copy(settings = settings, summaryTime = summaryAt(settings)) }
            }
        }
        viewModelScope.launch { loadLessons() }
        viewModelScope.launch {
            places.places.collect { all ->
                // Home and work first, as « Maison, Travail », then the shops without reminder.
                val quiet = all.filter { it.kind != PlaceKind.SHOP } + all.filter {
                    it.kind == PlaceKind.SHOP && !it.remindAfterVisit
                }
                state.update { it.copy(quietPlaces = quiet.map { place -> place.name }) }
            }
        }
    }

    fun onVisitReminders(on: Boolean) = save { it.copy(visitReminders = on) }

    fun onSummary(open: Boolean) = state.update { it.copy(choosingSummary = open) }

    fun onSummaryTime(time: LocalTime?) = save { it.copy(summaryTime = time) }

    fun onQuietHours(open: Boolean) = state.update { it.copy(choosingQuietHours = open) }

    fun onQuietFrom(hours: Int) = save { it.copy(quietFrom = Math.floorMod(it.quietFrom + hours, HOURS_PER_DAY)) }

    fun onQuietUntil(hours: Int) = save { it.copy(quietUntil = Math.floorMod(it.quietUntil + hours, HOURS_PER_DAY)) }

    fun onAskReset(open: Boolean) = state.update { it.copy(confirmingReset = open) }

    /** Forgets what was learnt, then tells it again from now: nothing yet. */
    fun onReset() {
        state.update { it.copy(confirmingReset = false) }
        viewModelScope.launch {
            learner.reset(clock.instant())
            loadLessons()
        }
    }

    fun onDailyCap(step: Int) = save { it.copy(dailyCap = (it.dailyCap + step).coerceIn(MIN_CAP, MAX_CAP)) }

    // Shown at once, saved behind.
    private fun save(change: (ReminderSettings) -> ReminderSettings) {
        val settings = change(state.value.settings)
        state.update { it.copy(settings = settings) }
        viewModelScope.launch { preferences.saveReminders(settings) }
    }

    // What was learnt since the last start from zero, over the window the bandit learns from.
    private suspend fun loadLessons() {
        val now = clock.instant()
        val since = listOfNotNull(now - LEARNING_WINDOW, preferences.learningSince.first()).max()
        val answers = log.trainingSet(since)
        val times = log.answerTimes(since)
        val hours = times.map { it.atZone(clock.zone).hour }
        val lessons = listOfNotNull(answerHours(hours)) + placeLessons(log.placeSpendRates(since))
        // From the first answer: a week of answers is one week, whatever the window.
        val weeks = times.firstOrNull()?.let { Duration.between(it, now).toDays().toInt() / DAYS_PER_WEEK } ?: 0
        state.update { it.copy(lessons = lessons, answers = answers.size, weeks = weeks) }
    }

    // The two hours in a row the user answers most in, once 10 answers show them.
    private fun answerHours(hours: List<Int>): Lesson.AnswerHours? {
        if (hours.size < KNOWN_ANSWERS) return null
        val from = (0 until HOURS_PER_DAY).maxBy { start ->
            hours.count { it == start || it == (start + 1) % HOURS_PER_DAY }
        }
        return Lesson.AnswerHours(from = from, until = (from + 2) % HOURS_PER_DAY)
    }

    // The shop visited 5 times at least that leads to an expense most often, and the one that leads to one least.
    private fun placeLessons(rates: List<PlaceSpendRate>): List<Lesson> {
        val known = rates.filter { it.visits >= KNOWN_VISITS }
        val often = known.filter { it.withExpense >= it.visits * OFTEN }.maxByOrNull {
            it.withExpense.toDouble() /
                it.visits
        }
        val rarely = known.filter { it.withExpense <= it.visits * RARE }.minByOrNull {
            it.withExpense.toDouble() /
                it.visits
        }
        return listOfNotNull(
            often?.let {
                Lesson.OftenSpends(
                    place = it.name,
                    timesInTen = Math.round(
                        it.withExpense * TEN.toDouble() / it.visits,
                    ).toInt(),
                )
            },
            rarely?.let { Lesson.RarelySpends(place = it.name) },
        )
    }

    // The time chosen, or the one the user's answers of the last month lead to, as the summary computes it.
    private suspend fun summaryAt(settings: ReminderSettings): LocalTime {
        val since = clock.instant() - ANSWERS_WINDOW
        val answers = log.answerTimes(since).map { it.atZone(clock.zone).toLocalTime() }
        return summaryTime.at(chosen = settings.summaryTime, answers = answers)
    }

    companion object {
        /** Fewest reminders a day: one, else the reminders would be off. */
        const val MIN_CAP = 1

        /** Most reminders a day: more would be noise. */
        const val MAX_CAP = 10

        private const val HOURS_PER_DAY = 24
        private val ANSWERS_WINDOW: Duration = Duration.ofDays(30)

        // As the bandit: the last 90 days.
        private val LEARNING_WINDOW: Duration = Duration.ofDays(90)
        private const val KNOWN_ANSWERS = 10
        private const val KNOWN_VISITS = 5
        private const val OFTEN = 0.7
        private const val RARE = 0.2
        private const val TEN = 10
        private const val DAYS_PER_WEEK = 7
    }
}
