package com.cauri.core.data.model

/** What happened after a decision of the reminder engine: the reason of its reward. */
enum class ReminderOutcome {
    /** The card was confirmed. */
    CONFIRMED,

    /** The user said « Rien dépensé ». */
    NOTHING_SPENT,

    /** The card was set aside, not an expense. */
    DISMISSED,

    /** The expense was typed by hand, or found in a cash count, after a skip. */
    MISSED,

    /** No answer in time. */
    TIMEOUT,
}
