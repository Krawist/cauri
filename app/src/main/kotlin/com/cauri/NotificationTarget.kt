package com.cauri

import com.cauri.core.capture.health.CaptureHealth
import com.cauri.feature.cash.reminder.CountReminders

/** A screen a notification opens the app on. */
internal enum class NotificationTarget {
    /** A count reminder: the cash count. */
    CASH_COUNT,

    /** The warning that the capture stopped: Réglages. */
    SETTINGS,
    ;

    companion object {
        /** The screen the intent [action] asks for; `null` for an ordinary launch. */
        fun of(action: String?): NotificationTarget? = when (action) {
            CountReminders.ACTION_COUNT_CASH -> CASH_COUNT
            CaptureHealth.ACTION_OPEN_SETTINGS -> SETTINGS
            else -> null
        }
    }
}
