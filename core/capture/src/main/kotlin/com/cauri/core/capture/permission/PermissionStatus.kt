package com.cauri.core.capture.permission

/**
 * What the user allowed Cauri to do, as Android reports it now.
 *
 * @property notificationAccess Whether Cauri may read the notifications of the operators, the banks and the SMS app.
 * @property notificationsAllowed Whether Cauri may show its own notifications: reminders and cards to confirm.
 * @property location How far Cauri may use the location, for place reminders.
 */
data class PermissionStatus(
    val notificationAccess: Boolean,
    val notificationsAllowed: Boolean,
    val location: LocationAccess,
)
