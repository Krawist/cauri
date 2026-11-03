package com.cauri.feature.settings

import com.cauri.core.data.model.CountReminderSettings

/** What the user can do on the settings screen. */
internal interface SettingsActions {
    fun onBack()

    fun onNotificationAccess()

    fun onNotifications()

    fun onLocation()

    fun onBattery()

    /** Opens « Mes lieux ». */
    fun onPlaces()

    /** Opens or closes the sheet of the cash count reminders. */
    fun onCountReminder(open: Boolean)

    fun onCountReminderChange(settings: CountReminderSettings)

    /** Follows the account of [provider] the user had chosen not to follow. */
    fun onFollowAccount(provider: String)
}
