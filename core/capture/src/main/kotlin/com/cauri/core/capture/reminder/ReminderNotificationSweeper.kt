package com.cauri.core.capture.reminder

/** Keeps the notifications of the cards in line with « À confirmer »: an answered card takes its notification off. */
interface ReminderNotificationSweeper {
    /** Follows the cards for as long as the app lives; call it once per process. */
    fun start()
}
