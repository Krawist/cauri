package com.cauri.feature.onboarding.security

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.security.AppLockService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The step « Protège ton argent » of the onboarding: the lock of Cauri by the phone's security (CAU-57). */
@HiltViewModel
internal class SecurityViewModel @Inject constructor(private val lock: AppLockService) : ViewModel() {
    private val state = MutableStateFlow(SecurityUiState(available = lock.available()))

    val uiState: StateFlow<SecurityUiState> = state.asStateFlow()

    init {
        viewModelScope.launch { lock.enabled.collect { on -> state.update { it.copy(enabled = on) } } }
    }

    /** Reads again whether the phone has a screen lock, such as back from its security settings. */
    fun refresh() = state.update { it.copy(available = lock.available()) }

    /** Turns the lock on once the user is recognised over [activity], then goes on with [onDone]. */
    fun onEnable(activity: Activity, onDone: () -> Unit) {
        viewModelScope.launch { if (lock.set(on = true, activity = activity)) onDone() }
    }
}
