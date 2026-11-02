package com.cauri.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// A count older than this shows its date instead of its weekday.
private const val WEEK_DAYS = 6L

/**
 * When the cash was counted, as people say it: « Compté aujourd'hui à 8 h 30 », « Compté hier à 20 h »,
 * « Compté lundi à 20 h », then « Compté le 12 octobre » after a week.
 */
@Composable
internal fun countedAtLabel(countedAt: Instant): String {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val time = countedAt.atZone(zone)
    val today = LocalDate.now(zone)
    val hourPattern = stringResource(
        if (time.minute == 0) R.string.home_time_pattern_hour else R.string.home_time_pattern,
    )
    val clock = DateTimeFormatter.ofPattern(hourPattern, locale).format(time)
    return when (val day = time.toLocalDate()) {
        today -> stringResource(R.string.home_counted_today, clock)
        today.minusDays(1) -> stringResource(R.string.home_counted_yesterday, clock)
        else -> if (day.isAfter(today.minusDays(WEEK_DAYS + 1))) {
            stringResource(
                R.string.home_counted_weekday,
                DateTimeFormatter.ofPattern("EEEE", locale).format(time),
                clock,
            )
        } else {
            stringResource(R.string.home_counted_date, DateTimeFormatter.ofPattern("d MMMM", locale).format(time))
        }
    }
}
