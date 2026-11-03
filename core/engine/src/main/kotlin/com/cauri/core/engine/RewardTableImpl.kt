package com.cauri.core.engine

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation for [RewardTable], « rewards-1 ». A notification answered is worth the most, one left without
 * answer or set aside costs; the summary earns and costs less, being quieter; a skip is right when nothing was spent
 * or the user answered alone, and the worst when the expense was missed. Public so that the app's Hilt modules can
 * bind it.
 */
@Singleton
class RewardTableImpl @Inject constructor() : RewardTable {
    override fun reward(action: ReminderAction, outcome: ReminderOutcome): Double =
        checkNotNull(TABLE.getValue(action)[outcome]) { "No reward for $action and $outcome" }

    private companion object {
        val TABLE: Map<ReminderAction, Map<ReminderOutcome, Double>> = mapOf(
            ReminderAction.NOTIFY_NOW to mapOf(
                ReminderOutcome.CONFIRMED to 1.0,
                ReminderOutcome.NOTHING_SPENT to 0.3,
                ReminderOutcome.DISMISSED to -0.5,
                ReminderOutcome.MISSED to -1.0,
                ReminderOutcome.TIMEOUT to -0.5,
            ),
            ReminderAction.EVENING_SUMMARY to mapOf(
                ReminderOutcome.CONFIRMED to 0.7,
                ReminderOutcome.NOTHING_SPENT to 0.2,
                ReminderOutcome.DISMISSED to -0.2,
                ReminderOutcome.MISSED to -1.0,
                ReminderOutcome.TIMEOUT to -0.3,
            ),
            ReminderAction.SKIP to mapOf(
                ReminderOutcome.CONFIRMED to 0.3,
                ReminderOutcome.NOTHING_SPENT to 0.5,
                ReminderOutcome.DISMISSED to 0.5,
                ReminderOutcome.MISSED to -1.0,
                ReminderOutcome.TIMEOUT to 0.0,
            ),
        )
    }
}
