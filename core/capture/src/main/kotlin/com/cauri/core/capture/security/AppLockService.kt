package com.cauri.core.capture.security

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/** The lock of Cauri by the phone's security (CAU-57), turned on or off only by the user once recognised. */
interface AppLockService {
    /** Whether the lock is on, again after each change. */
    val enabled: Flow<Boolean>

    /** Whether the phone has a screen lock, which the lock needs. */
    fun available(): Boolean

    /**
     * Turns the lock [on] or off once the user is recognised by the phone's security over [activity]: turning it off
     * asks too, even from inside the app. `false`, changing nothing, when they are not recognised.
     */
    suspend fun set(on: Boolean, activity: Activity): Boolean
}
