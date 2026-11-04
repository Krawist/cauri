package com.cauri.core.data.model

/** What a goal is. */
enum class GoalKind {
    /** An amount to put aside, such as a moto or the emergency fund, with a deadline or none. */
    SAVING,

    /** A contribution paid every month, a fixed cost, until the user's turn to take the pot. */
    TONTINE,

    /** An amount a month kept for a category, such as the family's emergencies. */
    ENVELOPE,
}
