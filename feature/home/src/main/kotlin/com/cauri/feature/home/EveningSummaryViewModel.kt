package com.cauri.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.repository.ActivityRepository
import com.cauri.core.data.service.ActivityQueryService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** « Ce soir », the evening summary: what was spent today, and of it in cash. [ConfirmationViewModel] has its cards. */
@HiltViewModel
internal class EveningSummaryViewModel @Inject constructor(
    activity: ActivityRepository,
    queries: ActivityQueryService,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val dayStart = today.atStartOfDay(clock.zone).toInstant()

    val uiState: StateFlow<EveningSummaryUiState> = activity.lines.map { lines ->
        val spending = queries.spending(lines.filter { !it.at.isBefore(dayStart) })
        EveningSummaryUiState(day = today, spent = spending.cash + spending.digital, cash = spending.cash)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_DELAY),
        initialValue = EveningSummaryUiState(day = today),
    )

    private companion object {
        // Kept a while after the screen leaves: a rotation does not reload it.
        const val STOP_DELAY = 5_000L
    }
}
