package com.cauri.feature.goals

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cauri.core.design.theme.CauriTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * A day from tomorrow on, [day] chosen first; « Aucune » gives `null` through [onDate]. The picker counts days in UTC
 * milliseconds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateDialog(day: LocalDate?, today: LocalDate, onDate: (LocalDate?) -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = day?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(today)

            override fun isSelectableYear(year: Int) = year >= today.year
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
                        onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
            ) { Text(text = stringResource(R.string.goals_date_choose), color = palette.accentText) }
        },
        dismissButton = {
            TextButton(onClick = { onDate(null) }) {
                Text(text = stringResource(R.string.goals_date_none), color = palette.muted)
            }
        },
        colors = colors,
    ) {
        DatePicker(state = picker, colors = colors)
    }
}
