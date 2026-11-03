package com.cauri.core.engine

/** What the reminder engine decides for a card to confirm. */
enum class ReminderAction {
    /** A notification at once. */
    NOTIFY_NOW,

    /** A line of the evening summary. */
    EVENING_SUMMARY,

    /** Nothing: the card waits in « À confirmer ». */
    SKIP,
}
