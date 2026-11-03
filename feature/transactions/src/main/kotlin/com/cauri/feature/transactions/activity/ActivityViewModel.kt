package com.cauri.feature.transactions.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivitySort
import com.cauri.core.data.repository.ActivityRepository
import com.cauri.core.data.service.ActivityQueryService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * « Activité »: the history grouped by day or sorted by amount, narrowed by kind of account, search and period, and
 * what was spent over the period.
 */
@HiltViewModel
internal class ActivityViewModel @Inject constructor(
    activity: ActivityRepository,
    private val queries: ActivityQueryService,
    private val clock: Clock,
) : ViewModel(),
    ActivityActions {
    private val filter = MutableStateFlow(ActivityFilter.ALL)
    private val query = MutableStateFlow<String?>(null)
    private val period = MutableStateFlow<ActivityPeriod>(ActivityPeriod.All)
    private val sort = MutableStateFlow(ActivitySort.DATE)

    val uiState: StateFlow<ActivityUiState> =
        combine(activity.lines, filter, query, period, sort) { lines, filter, query, period, sort ->
            val today = LocalDate.now(clock)
            val days = queries.days(period = period, today = today)
            // The whole history totals the current month.
            val spent = days ?: queries.days(period = ActivityPeriod.ThisMonth, today = today)
            val shown = lines.filter {
                it.within(days) && it.shownBy(filter) && queries.matches(line = it, query = query.orEmpty())
            }
            ActivityUiState(
                filter = filter,
                query = query,
                period = period,
                sort = sort,
                days = shown.grouped(sort),
                spending = queries.spending(lines.filter { it.within(spent) }),
                today = today,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_DELAY),
            initialValue = ActivityUiState(),
        )

    override fun onFilter(filter: ActivityFilter) {
        this.filter.value = filter
    }

    override fun onSearch(open: Boolean) {
        query.value = if (open) query.value.orEmpty() else null
    }

    override fun onQuery(query: String) {
        this.query.value = query
    }

    override fun onPeriod(period: ActivityPeriod) {
        this.period.value = period
    }

    override fun onSort(sort: ActivitySort) {
        this.sort.value = sort
    }

    // By day, newest first; or one group, largest amount first, the cash counts left out: they move no money.
    private fun List<ActivityLine>.grouped(sort: ActivitySort): List<ActivityDay> = when (sort) {
        ActivitySort.DATE -> groupBy { it.date() }.map { (date, lines) -> ActivityDay(date = date, lines = lines) }
        ActivitySort.AMOUNT -> filterIsInstance<ActivityLine.Operation>()
            .sortedByDescending { it.amount }
            .takeIf { it.isNotEmpty() }
            ?.let { listOf(ActivityDay(date = null, lines = it)) }
            .orEmpty()
    }

    private fun ActivityLine.date(): LocalDate = at.atZone(clock.zone).toLocalDate()

    private fun ActivityLine.within(days: ClosedRange<LocalDate>?) = days == null || date() in days

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
