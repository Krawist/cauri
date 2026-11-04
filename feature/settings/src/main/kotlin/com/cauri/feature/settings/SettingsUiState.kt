package com.cauri.feature.settings

import com.cauri.core.capture.permission.PermissionStatus
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.NewAccount

/**
 * What the settings screen shows.
 *
 * @property permissions Status of each permission.
 * @property batteryUnrestricted Whether the phone lets Cauri run without battery restriction.
 * @property countReminder The user's choice for the cash count reminders.
 * @property choosingReminder Whether the sheet to change [countReminder] is open.
 * @property ignoredAccounts Accounts whose messages the user chose not to follow, by name.
 * @property lockEnabled Whether Cauri is locked by the phone's security (CAU-57).
 * @property lockAvailable Whether the phone has a screen lock, which the lock of Cauri needs.
 */
internal data class SettingsUiState(
    val permissions: PermissionStatus,
    val batteryUnrestricted: Boolean,
    val countReminder: CountReminderSettings = CountReminderSettings(
        frequency = CountReminderFrequency.WHEN_USEFUL,
        hour = DEFAULT_REMINDER_HOUR,
    ),
    val choosingReminder: Boolean = false,
    val ignoredAccounts: List<NewAccount> = emptyList(),
    val lockEnabled: Boolean = false,
    val lockAvailable: Boolean = false,
)

// Until the stored choice is read; the same default as the preferences.
private const val DEFAULT_REMINDER_HOUR = 20
