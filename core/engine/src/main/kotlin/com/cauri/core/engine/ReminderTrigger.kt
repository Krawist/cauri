package com.cauri.core.engine

/** What calls the reminder engine: its three points of decision. */
enum class ReminderTrigger {
    /** The user left a shop with nothing paid there: what was spent, in cash? */
    VISIT_END,

    /** An operation read in a notification: when to ask about it, not whether. */
    DETECTED_OPERATION,

    /** Cash withdrawn: what it will pay for shows in the next count. */
    WITHDRAWAL,
}
