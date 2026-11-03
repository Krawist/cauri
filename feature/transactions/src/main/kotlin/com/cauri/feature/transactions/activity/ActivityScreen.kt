package com.cauri.feature.transactions.activity

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivitySort
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.Spending
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.component.pressScale
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FilterHeight = 36.dp
private val FilterRadius = 22.dp
private val GroupRadius = 22.dp

// Above the floating bottom bar of the app.
private val BottomSpace = 120.dp

/**
 * « Activité »: the filters « Tout », « Cash », « Mobile » and « Banque », what was spent this month in cash and
 * digital, then the history grouped by day, the cash counts with their gap. A line opens the edit sheet through
 * [onEditTransaction].
 */
@Composable
internal fun ActivityScreen(onEditTransaction: (Long) -> Unit, viewModel: ActivityViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ActivityContent(state = state, actions = viewModel, onEditTransaction = onEditTransaction)
}

@Composable
private fun ActivityContent(state: ActivityUiState, actions: ActivityActions, onEditTransaction: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(
            start = CauriSpacing.xl,
            top = CauriSpacing.xxl,
            end = CauriSpacing.xl,
            bottom = BottomSpace,
        ),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
    ) {
        item { ActivityHeader(query = state.query, actions = actions) }
        item { Filters(selected = state.filter, onFilter = actions::onFilter) }
        item { ActivityOptionsChip(period = state.period, sort = state.sort, actions = actions) }
        item { Spending(spending = state.spending) }
        if (state.days.isEmpty()) {
            item {
                // Nothing yet, or nothing that answers the search and the period.
                val narrowed = !state.query.isNullOrBlank() || state.period != ActivityPeriod.All
                Text(
                    text = stringResource(
                        if (narrowed) R.string.transactions_activity_no_match else R.string.transactions_activity_empty,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CauriTheme.palette.muted,
                    modifier = Modifier.padding(top = CauriSpacing.s),
                )
            }
        }
        items(items = state.days, key = { it.date?.toEpochDay() ?: Long.MIN_VALUE }) { day ->
            // A day that comes or goes with a filter or a search fades, the others move into place.
            Box(
                modifier = Modifier.animateItem(
                    fadeInSpec = CauriTheme.motion.tween(),
                    placementSpec = CauriTheme.motion.tween(),
                    fadeOutSpec = CauriTheme.motion.tween(),
                ),
            ) { Day(day = day, today = state.today, onEditTransaction = onEditTransaction) }
        }
    }
}

// The four filters on a glass track, the chosen one raised.
@Composable
private fun Filters(selected: ActivityFilter, onFilter: (ActivityFilter) -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(FilterRadius), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(CauriSpacing.xs)) {
            ActivityFilter.entries.forEach { filter ->
                val chosen = filter == selected
                // The raised segment glides to the filter chosen.
                val fill by animateColorAsState(
                    if (chosen) palette.segment else palette.segment.copy(alpha = 0f),
                    CauriTheme.motion.tween(),
                    label = "segment",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(FilterHeight)
                        .pressScale()
                        .clip(CircleShape)
                        .background(fill, CircleShape)
                        .selectable(selected = chosen, role = Role.Tab, onClick = {
                            if (!chosen) haptics.select()
                            onFilter(filter)
                        }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            when (filter) {
                                ActivityFilter.ALL -> R.string.transactions_activity_all
                                ActivityFilter.CASH -> R.string.transactions_activity_cash
                                ActivityFilter.MOBILE -> R.string.transactions_activity_mobile
                                ActivityFilter.BANK -> R.string.transactions_activity_bank
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = if (chosen) palette.ink else palette.muted,
                    )
                }
            }
        }
    }
}

// « Cash dépensé » in gold and « Numérique dépensé », side by side.
@Composable
private fun Spending(spending: Spending) {
    val palette = CauriTheme.palette
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        listOf(
            Triple(R.string.transactions_activity_cash_spent, spending.cash, palette.goldText),
            Triple(R.string.transactions_activity_digital_spent, spending.digital, palette.muted),
        ).forEach { (label, amount, color) ->
            GlassSurface(shape = RoundedCornerShape(CauriRadius.tile), modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
                    verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs),
                ) {
                    Text(
                        text = stringResource(label),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = color,
                    )
                    // It rolls to the total of a new period or a new expense.
                    RollingNumber(value = amount) { shown ->
                        Text(
                            text = stringResource(R.string.transactions_amount, formatFcfa(shown)),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.ink,
                        )
                    }
                }
            }
        }
    }
}

// « Aujourd'hui », « Hier » or « Lundi 2 novembre », over the day's lines on one glass card; no day when sorted by
// amount.
@Composable
private fun Day(day: ActivityDay, today: LocalDate?, onEditTransaction: (Long) -> Unit) {
    val palette = CauriTheme.palette
    val locale = LocalConfiguration.current.locales[0]
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp)) {
        // Sorted by amount, the lines have no day.
        day.date?.let { date ->
            Text(
                text = when (date) {
                    today -> stringResource(R.string.transactions_activity_today)
                    today?.minusDays(1) -> stringResource(R.string.transactions_activity_yesterday)
                    else -> DateTimeFormatter.ofPattern("EEEE d MMMM", locale).format(date)
                        .replaceFirstChar { it.titlecase(locale) }
                },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.muted,
                modifier = Modifier.padding(top = CauriSpacing.xs),
            )
        }
        GlassSurface(shape = RoundedCornerShape(GroupRadius), modifier = Modifier.fillMaxWidth()) {
            Column {
                day.lines.forEachIndexed { index, line ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = CauriSpacing.l)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(palette.chip),
                        )
                    }
                    when (line) {
                        is ActivityLine.Operation -> OperationRow(line = line, onClick = {
                            onEditTransaction(line.transactionId)
                        })
                        is ActivityLine.CashCount -> CashCountRow(line = line)
                    }
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ActivityContentPreview() {
    val today = LocalDate.now()
    val noon = today.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant()
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            ActivityContent(
                state = ActivityUiState(
                    days = listOf(
                        ActivityDay(
                            date = today,
                            lines = listOf(
                                ActivityLine.CashCount(
                                    at = noon,
                                    countId = 1,
                                    amount = 3_000,
                                    expectedAmount = 7_500,
                                    expenses = 3,
                                ),
                                previewOperation(at = noon, note = "Moto-taxi", category = "Transport"),
                                previewOperation(at = noon, note = null, category = null)
                                    .copy(status = ActivityStatus.PENDING, counterparty = "BOULANGERIE SAKER"),
                            ),
                        ),
                        ActivityDay(
                            date = today.minusDays(1),
                            lines = listOf(
                                previewOperation(at = noon.minus(Duration.ofDays(1)), note = null, category = null)
                                    .copy(
                                        type = PendingType.TRANSFER,
                                        accountName = "MTN MoMo",
                                        accountKind = AccountKind.MOBILE_MONEY,
                                        toAccountName = "Cash",
                                        toAccountKind = AccountKind.CASH,
                                        amount = 20_000,
                                    ),
                            ),
                        ),
                    ),
                    spending = Spending(cash = 86_400, digital = 55_900),
                    today = today,
                ),
                actions = PreviewActivityActions,
                onEditTransaction = {},
            )
        }
    }
}

private fun previewOperation(at: Instant, note: String?, category: String?) = ActivityLine.Operation(
    at = at,
    transactionId = 1,
    type = PendingType.EXPENSE,
    status = ActivityStatus.CONFIRMED,
    amount = 500,
    accountName = "Cash",
    accountKind = AccountKind.CASH,
    toAccountName = null,
    toAccountKind = null,
    categoryName = category,
    parts = 1,
    counterparty = null,
    note = note,
    fees = 0,
    fee = false,
    reconciled = false,
)

/** Actions that do nothing, for previews. */
private object PreviewActivityActions : ActivityActions {
    override fun onFilter(filter: ActivityFilter) = Unit

    override fun onSearch(open: Boolean) = Unit

    override fun onQuery(query: String) = Unit

    override fun onPeriod(period: ActivityPeriod) = Unit

    override fun onSort(sort: ActivitySort) = Unit
}
