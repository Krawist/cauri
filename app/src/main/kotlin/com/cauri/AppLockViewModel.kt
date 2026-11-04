package com.cauri

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The lock of Cauri (CAU-57): once on, the app is locked when it starts, and again when it comes back after more than
 * [RELOCK_AFTER_MILLIS] away; turned on from Réglages, it does not lock the user who just turned it on.
 */
@HiltViewModel
internal class AppLockViewModel @Inject constructor(preferences: UserPreferencesRepository) : ViewModel() {
    private val state = MutableStateFlow(AppLockUiState())

    val uiState: StateFlow<AppLockUiState> = state.asStateFlow()

    // When the app went out of sight, in milliseconds of the phone's uptime; `null` while it is in sight.
    private var leftAt: Long? = null

    init {
        viewModelScope.launch {
            preferences.appLock.collect { on ->
                state.update { current ->
                    // The first reading locks a start; later changes come from the user, already recognised.
                    current.copy(enabled = on, locked = on && (current.enabled == null || current.locked))
                }
            }
        }
    }

    /** The app went out of sight at [at], in milliseconds of the phone's uptime. */
    fun onLeft(at: Long) {
        leftAt = at
    }

    /** The app came back in sight at [at]: locked again when it was away for more than a minute. */
    fun onBack(at: Long) {
        val away = leftAt?.let { at - it }
        leftAt = null
        if (state.value.enabled == true && away != null && away > RELOCK_AFTER_MILLIS) {
            state.update { it.copy(locked = true) }
        }
    }

    /** The user was recognised by the phone's security. */
    fun onUnlocked() = state.update { it.copy(locked = false) }

    companion object {
        /** Time away after which the app asks again: a minute, to answer an SMS or copy an amount in peace. */
        const val RELOCK_AFTER_MILLIS = 60_000L
    }
}
