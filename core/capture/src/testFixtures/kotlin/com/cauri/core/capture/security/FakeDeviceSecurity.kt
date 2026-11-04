package com.cauri.core.capture.security

import android.app.Activity
import android.content.Intent

/** [DeviceSecurity] for tests: the test sets whether the phone has a screen lock and whether the user is recognised. */
class FakeDeviceSecurity : DeviceSecurity {
    var screenLock = true

    var recognised = true

    var asked = 0

    override fun available() = screenLock

    override suspend fun authenticate(activity: Activity): Boolean {
        asked++
        return screenLock && recognised
    }

    override fun securitySettings() = Intent("security")
}
