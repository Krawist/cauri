package com.cauri.core.capture.security

import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow

/** [AppLockService] for tests: the test sets whether the phone has a screen lock and whether the user is recognised. */
class FakeAppLockService : AppLockService {
    override val enabled = MutableStateFlow(false)

    var screenLock = true

    var recognised = true

    override fun available() = screenLock

    override suspend fun set(on: Boolean, activity: Activity): Boolean {
        val done = screenLock && recognised
        if (done) enabled.value = on
        return done
    }
}
