package com.cauri.feature.settings.reminders

import com.cauri.core.data.model.ReminderSettings

/**
 * What the smart reminders screen shows.
 *
 * @property settings The user's limits on the reminders.
 * @property quietPlaces Names of the places that never bring a reminder: home, work and the shops without one.
 * @property choosingQuietHours Whether the sheet of the quiet hours is open.
 */
internal data class RemindersUiState(
    val settings: ReminderSettings = ReminderSettings(
        quietFrom = DEFAULT_QUIET_FROM,
        quietUntil = DEFAULT_QUIET_UNTIL,
        dailyCap = DEFAULT_DAILY_CAP,
        visitReminders = true,
    ),
    val quietPlaces: List<String> = emptyList(),
    val choosingQuietHours: Boolean = false,
)

// Until the stored choice is read; the same defaults as the preferences.
private const val DEFAULT_QUIET_FROM = 22
private const val DEFAULT_QUIET_UNTIL = 7
private const val DEFAULT_DAILY_CAP = 4
