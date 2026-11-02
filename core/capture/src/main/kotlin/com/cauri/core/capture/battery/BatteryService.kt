package com.cauri.core.capture.battery

import android.content.Intent

/** Whether the phone may stop Cauri to save battery, and where the user changes it. */
interface BatteryService {
    /** Whether Cauri runs without battery restriction (« Non restreinte »), as Android reports it now. */
    fun unrestricted(): Boolean

    /** System screen of Cauri's details, whose « Batterie » entry sets its restriction. */
    fun appSettings(): Intent
}
