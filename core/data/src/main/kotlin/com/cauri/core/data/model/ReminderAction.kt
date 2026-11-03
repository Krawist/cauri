package com.cauri.core.data.model

/** What the reminder engine decided. */
enum class ReminderAction {
    /** A notification at once. */
    NOTIFY_NOW,

    /** A line of the evening summary. */
    EVENING_SUMMARY,

    /** Nothing: the card waits in « À confirmer ». */
    SKIP,
}
