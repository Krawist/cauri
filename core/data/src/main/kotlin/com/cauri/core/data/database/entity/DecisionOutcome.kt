package com.cauri.core.data.database.entity

/** What happened after a decision; the reward reason used at training time. */
internal enum class DecisionOutcome {
    CONFIRMED,

    /** The user said « Rien dépensé ». */
    NOTHING_SPENT,
    DISMISSED,

    /** The expense was entered by hand, or showed up in a cash gap, after a skip. */
    MISSED,

    /** No reaction in time. */
    TIMEOUT,
}
