package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Home screen; for now only the initial of the profile button. */
@HiltViewModel
internal class HomeViewModel @Inject constructor(preferences: UserPreferencesRepository) : ViewModel() {
    /** First letter of the user's name, in capitals; empty until the profile is read. */
    val initial: StateFlow<String> = preferences.profile
        .map { profile -> profile?.displayName?.take(1)?.uppercase().orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), "")

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
