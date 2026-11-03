package com.cauri.feature.transactions.edit

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** The day, then the hour the money moved; never later than now. */
@Composable
internal fun MomentDialog(moment: Instant, onMoment: (Instant) -> Unit, onDismiss: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val current = moment.atZone(zone)
    var day by remember { mutableStateOf<LocalDate?>(null) }
    val chosenDay = day
    if (chosenDay == null) {
        DayDialog(day = current.toLocalDate(), today = LocalDate.now(zone), onDay = { day = it }, onDismiss = onDismiss)
    } else {
        HourDialog(
            hour = current.hour,
            minute = current.minute,
            onHour = { hour, minute ->
                onMoment(minOf(chosenDay.atTime(hour, minute).atZone(zone).toInstant(), Instant.now()))
            },
            onDismiss = onDismiss,
        )
    }
}

// A day up to [today]; the picker counts days in UTC milliseconds.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDialog(day: LocalDate, today: LocalDate, onDay: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(today)

            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )
    val colors = DatePickerDefaults.colors(
        containerColor = palette.background,
        selectedDayContainerColor = palette.accent,
        selectedDayContentColor = palette.onAccent,
        todayDateBorderColor = palette.accent,
        todayContentColor = palette.accentText,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    picker.selectedDateMillis?.let {
                        onDay(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
            ) { Text(text = stringResource(R.string.transactions_edit_next), color = palette.accentText) }
        },
        dismissButton = { Cancel(onDismiss) },
        colors = colors,
    ) {
        DatePicker(state = picker, colors = colors)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourDialog(hour: Int, minute: Int, onHour: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    val clock = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.background,
        text = {
            TimePicker(
                state = clock,
                colors = TimePickerDefaults.colors(
                    selectorColor = palette.accent,
                    timeSelectorSelectedContainerColor = palette.accentSoft,
                    timeSelectorSelectedContentColor = palette.accentText,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onHour(clock.hour, clock.minute) }) {
                Text(text = stringResource(R.string.transactions_edit_ok), color = palette.accentText)
            }
        },
        dismissButton = { Cancel(onDismiss) },
    )
}

@Composable
private fun Cancel(onDismiss: () -> Unit) {
    TextButton(onClick = onDismiss) {
        Text(text = stringResource(R.string.transactions_edit_cancel), color = CauriTheme.palette.muted)
    }
}
