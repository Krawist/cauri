package com.cauri.core.engine

/**
 * The rewards of the decisions, applied when the bandit trains rather than when the outcome is logged: a new table
 * gets a new [VERSION] and the log, which keeps the outcomes, is learnt again with it.
 */
interface RewardTable {
    /** The reward of [action] followed by [outcome], from -1 to 1. */
    fun reward(action: ReminderAction, outcome: ReminderOutcome): Double

    companion object {
        /** Version of the table, kept with each model trained with it. */
        const val VERSION = "rewards-1"
    }
}
