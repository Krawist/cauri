package com.cauri.core.capture.permission

import android.content.Intent

/** [PermissionService] for tests, whose [status] the test changes as Android would. */
class FakePermissionService : PermissionService {
    var status = PermissionStatus(
        notificationAccess = false,
        notificationsAllowed = false,
        location = LocationAccess.DENIED,
    )

    override fun status() = status

    override fun notificationAccessSettings() = Intent()

    override fun notificationSettings() = Intent()

    override fun appSettings() = Intent()
}
