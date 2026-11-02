package com.cauri.core.data.database.entity

/** What the reminder engine decided. */
internal enum class ReminderAction {
    NOTIFY_NOW,
    EVENING_SUMMARY,
    SKIP,
}
