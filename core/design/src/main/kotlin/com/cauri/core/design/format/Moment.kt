package com.cauri.core.design.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.cauri.core.design.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// A moment older than this shows its date instead of its weekday.
private const val WEEK_DAYS = 6L

/**
 * A past moment as people say it: « aujourd'hui à 8 h 30 », « hier à 20 h », « lundi à 20 h », then « le 12 octobre »
 * after a week. Meant to follow a verb: « Compté hier à 20 h ».
 */
@Composable
fun formatMoment(moment: Instant): String {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val time = moment.atZone(zone)
    val today = LocalDate.now(zone)
    val hourPattern = stringResource(
        if (time.minute == 0) R.string.moment_time_pattern_hour else R.string.moment_time_pattern,
    )
    val clock = DateTimeFormatter.ofPattern(hourPattern, locale).format(time)
    return when (val day = time.toLocalDate()) {
        today -> stringResource(R.string.moment_today, clock)
        today.minusDays(1) -> stringResource(R.string.moment_yesterday, clock)
        else -> if (day.isAfter(today.minusDays(WEEK_DAYS + 1))) {
            stringResource(R.string.moment_weekday, DateTimeFormatter.ofPattern("EEEE", locale).format(time), clock)
        } else {
            stringResource(R.string.moment_date, DateTimeFormatter.ofPattern("d MMMM", locale).format(time))
        }
    }
}
