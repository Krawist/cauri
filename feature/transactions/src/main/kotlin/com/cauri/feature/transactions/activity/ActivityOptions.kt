package com.cauri.feature.transactions.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cauri.core.data.model.ActivityPeriod
import com.cauri.core.data.model.ActivitySort
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R
import com.cauri.feature.transactions.entry.EntryChip
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** « Ce mois · Par date »: the period and the order shown; a tap opens the sheet that changes them. */
@Composable
internal fun ActivityOptionsChip(period: ActivityPeriod, sort: ActivitySort, actions: ActivityActions) {
    var open by rememberSaveable { mutableStateOf(false) }
    EntryChip(
        label = stringResource(R.string.transactions_activity_options, periodLabel(period), sortLabel(sort)),
        // Outlined once narrowed or sorted otherwise than by default.
        selected = period != ActivityPeriod.All || sort != ActivitySort.DATE,
        onClick = { open = true },
    )
    if (open) ActivityOptionsSheet(period = period, sort = sort, actions = actions, onDismiss = { open = false })
}

// « Période »: the whole history, this week, this month, last month or two days; « Trier »: by date or amount.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ActivityOptionsSheet(
    period: ActivityPeriod,
    sort: ActivitySort,
    actions: ActivityActions,
    onDismiss: () -> Unit,
) {
    val palette = CauriTheme.palette
    var choosingDays by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.sheet.compositeOver(palette.background),
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        ) {
            Title(text = stringResource(R.string.transactions_activity_period))
            PeriodChips(period = period, onPeriod = actions::onPeriod, onChooseDays = { choosingDays = true })
            Title(
                text = stringResource(R.string.transactions_activity_sort),
                modifier = Modifier.padding(top = CauriSpacing.s),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
                ActivitySort.entries.forEach { choice ->
                    EntryChip(label = sortLabel(choice), selected = choice == sort, onClick = {
                        actions.onSort(choice)
                    })
                }
            }
        }
    }
    if (choosingDays) {
        DaysDialog(
            period = period as? ActivityPeriod.Between,
            onDays = {
                actions.onPeriod(it)
                choosingDays = false
            },
            onDismiss = { choosingDays = false },
        )
    }
}

// The periods, the two days chosen named on the last chip.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodChips(period: ActivityPeriod, onPeriod: (ActivityPeriod) -> Unit, onChooseDays: () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        listOf(ActivityPeriod.All, ActivityPeriod.ThisWeek, ActivityPeriod.ThisMonth, ActivityPeriod.LastMonth)
            .forEach { choice ->
                EntryChip(
                    label = periodLabel(choice),
                    selected = choice == period,
                    onClick = { onPeriod(choice) },
                )
            }
        EntryChip(
            label = if (period is ActivityPeriod.Between) {
                periodLabel(period)
            } else {
                stringResource(R.string.transactions_activity_period_choose)
            },
            selected = period is ActivityPeriod.Between,
            onClick = onChooseDays,
        )
    }
}

// Two days, never after today; the picker counts days in UTC milliseconds.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaysDialog(
    period: ActivityPeriod.Between?,
    onDays: (ActivityPeriod.Between) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = CauriTheme.palette
    val today = LocalDate.now()
    val picker = rememberDateRangePickerState(
        initialSelectedStartDateMillis = period?.from?.millis(),
        initialSelectedEndDateMillis = period?.to?.millis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !utcTimeMillis.date().isAfter(today)

            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )
    val colors = DatePickerDefaults.colors(
        containerColor = palette.background,
        selectedDayContainerColor = palette.accent,
        selectedDayContentColor = palette.onAccent,
        dayInSelectionRangeContainerColor = palette.accentSoft,
        dayInSelectionRangeContentColor = palette.ink,
        todayDateBorderColor = palette.accent,
        todayContentColor = palette.accentText,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val from = picker.selectedStartDateMillis ?: return@TextButton
                    val to = picker.selectedEndDateMillis ?: from
                    onDays(ActivityPeriod.Between(from = from.date(), to = to.date()))
                },
            ) { Text(text = stringResource(R.string.transactions_edit_ok), color = palette.accentText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.transactions_edit_cancel), color = palette.muted)
            }
        },
        colors = colors,
    ) {
        DateRangePicker(state = picker, colors = colors, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Title(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = CauriTheme.palette.ink,
        modifier = modifier,
    )
}

@Composable
private fun periodLabel(period: ActivityPeriod): String = when (period) {
    ActivityPeriod.All -> stringResource(R.string.transactions_activity_period_all)
    ActivityPeriod.ThisWeek -> stringResource(R.string.transactions_activity_period_week)
    ActivityPeriod.ThisMonth -> stringResource(R.string.transactions_activity_period_month)
    ActivityPeriod.LastMonth -> stringResource(R.string.transactions_activity_period_last_month)
    is ActivityPeriod.Between -> {
        val format = DateTimeFormatter.ofPattern("d MMM", LocalConfiguration.current.locales[0])
        if (period.from == period.to) {
            format.format(period.from)
        } else {
            stringResource(
                R.string.transactions_activity_period_days,
                format.format(period.from),
                format.format(period.to),
            )
        }
    }
}

@Composable
private fun sortLabel(sort: ActivitySort): String = stringResource(
    when (sort) {
        ActivitySort.DATE -> R.string.transactions_activity_sort_date
        ActivitySort.AMOUNT -> R.string.transactions_activity_sort_amount
    },
)

private fun LocalDate.millis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.date(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
