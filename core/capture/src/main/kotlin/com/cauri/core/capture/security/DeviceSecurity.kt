package com.cauri.core.capture.security

import android.app.Activity
import android.content.Intent

/** The phone's own security (fingerprint, face or the screen lock's code), which locks Cauri when the user wants it. */
interface DeviceSecurity {
    /** Whether the phone has a screen lock: without one, Cauri cannot be locked. */
    fun available(): Boolean

    /**
     * Shows Android's dialog over [activity], the fingerprint, the face or the screen lock's code; `true` once the
     * user is recognised, `false` when they cancel it or the phone has no screen lock.
     */
    suspend fun authenticate(activity: Activity): Boolean

    /** System screen where the user sets a screen lock. */
    fun securitySettings(): Intent
}
