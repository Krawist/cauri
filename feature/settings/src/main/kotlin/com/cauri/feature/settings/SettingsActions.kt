package com.cauri.feature.settings

/** What the user can do on the settings screen. */
internal interface SettingsActions {
    fun onBack()

    fun onNotificationAccess()

    fun onNotifications()

    fun onLocation()

    fun onBattery()
}
