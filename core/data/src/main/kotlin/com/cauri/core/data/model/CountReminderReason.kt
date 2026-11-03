package com.cauri.core.data.model

/** Why Cauri suggests counting the cash now; the notification says it. */
enum class CountReminderReason {
    /** Cash was withdrawn today. */
    WITHDRAWAL,

    /** Several cash expenses were recorded since the last count. */
    EXPENSES,

    /** The last count is a few days old. */
    DAYS_WITHOUT_COUNT,

    /** The user asked for a reminder every evening, and none of the above applies. */
    EVENING,
}
