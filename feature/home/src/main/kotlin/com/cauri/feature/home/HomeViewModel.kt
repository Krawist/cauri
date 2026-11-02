package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.CashCountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Home screen: the profile button and the estimated cash in pocket, kept up to date. */
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    preferences: UserPreferencesRepository,
    cashCounts: CashCountRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(preferences.profile, cashCounts.balance) { profile, cash ->
        HomeUiState(initial = profile?.displayName?.take(1)?.uppercase().orEmpty(), cash = cash)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
