package com.cauri.feature.transactions.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** « Activité »: the history grouped by day, filtered by kind of account, and what was spent this month. */
@HiltViewModel
internal class ActivityViewModel @Inject constructor(activity: ActivityRepository, private val clock: Clock) :
    ViewModel() {
    private val filter = MutableStateFlow(ActivityFilter.ALL)

    val uiState: StateFlow<ActivityUiState> =
        combine(activity.lines, activity.monthSpending, filter) { lines, spending, filter ->
            ActivityUiState(
                filter = filter,
                days = lines.filter { it.shownBy(filter) }
                    .groupBy { it.at.atZone(clock.zone).toLocalDate() }
                    .map { (date, lines) -> ActivityDay(date = date, lines = lines) },
                spending = spending,
                today = LocalDate.now(clock),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_DELAY),
            initialValue = ActivityUiState(),
        )

    /** Shows the accounts of [filter]. */
    fun onFilter(filter: ActivityFilter) {
        this.filter.value = filter
    }

    // A transfer shows on the filter of either of its accounts; a cash count with the cash.
    private fun ActivityLine.shownBy(filter: ActivityFilter): Boolean {
        if (filter == ActivityFilter.ALL) return true
        val kinds = when (this) {
            is ActivityLine.CashCount -> listOf(AccountKind.CASH)
            is ActivityLine.Operation -> listOfNotNull(accountKind, toAccountKind)
        }
        return kinds.any { it.filter() == filter }
    }

    private fun AccountKind.filter() = when (this) {
        AccountKind.CASH -> ActivityFilter.CASH
        AccountKind.MOBILE_MONEY, AccountKind.PREPAID_CARD -> ActivityFilter.MOBILE
        AccountKind.BANK -> ActivityFilter.BANK
    }

    private companion object {
        // Kept a while after the screen leaves: a rotation does not reload it.
        const val STOP_DELAY = 5_000L
    }
}
