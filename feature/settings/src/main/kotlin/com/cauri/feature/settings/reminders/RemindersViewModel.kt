package com.cauri.feature.settings.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.ReminderSettings
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « Rappels intelligents »: the guardrails the user sets on the reminders (after visits or not, the quiet hours, the
 * daily cap) and the places that never bring one. Each change is saved at once.
 */
@HiltViewModel
internal class RemindersViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    places: PlaceRepository,
) : ViewModel() {
    private val state = MutableStateFlow(RemindersUiState())

    val uiState: StateFlow<RemindersUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.reminders.collect { settings -> state.update { it.copy(settings = settings) } }
        }
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

    fun onQuietHours(open: Boolean) = state.update { it.copy(choosingQuietHours = open) }

    fun onQuietFrom(hours: Int) = save { it.copy(quietFrom = Math.floorMod(it.quietFrom + hours, HOURS_PER_DAY)) }

    fun onQuietUntil(hours: Int) = save { it.copy(quietUntil = Math.floorMod(it.quietUntil + hours, HOURS_PER_DAY)) }

    fun onDailyCap(step: Int) = save { it.copy(dailyCap = (it.dailyCap + step).coerceIn(MIN_CAP, MAX_CAP)) }

    // Shown at once, saved behind.
    private fun save(change: (ReminderSettings) -> ReminderSettings) {
        val settings = change(state.value.settings)
        state.update { it.copy(settings = settings) }
        viewModelScope.launch { preferences.saveReminders(settings) }
    }

    companion object {
        /** Fewest reminders a day: one, else the reminders would be off. */
        const val MIN_CAP = 1

        /** Most reminders a day: more would be noise. */
        const val MAX_CAP = 10

        private const val HOURS_PER_DAY = 24
    }
}
