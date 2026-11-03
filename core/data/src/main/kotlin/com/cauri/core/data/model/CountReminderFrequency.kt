package com.cauri.core.data.model

/** How often Cauri suggests counting the cash, in the evening. */
enum class CountReminderFrequency {
    /** Only after a withdrawal, several cash expenses or a few days without a count. */
    WHEN_USEFUL,

    /** Every evening the cash was not counted yet. */
    EVERY_EVENING,
    NEVER,
}
