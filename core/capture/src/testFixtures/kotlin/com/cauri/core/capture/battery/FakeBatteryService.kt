package com.cauri.core.capture.battery

import android.content.Intent

/** [BatteryService] for tests, whose [restricted] state the test changes as Android would. */
class FakeBatteryService : BatteryService {
    var restricted = true

    override fun unrestricted() = !restricted

    override fun appSettings() = Intent()
}
