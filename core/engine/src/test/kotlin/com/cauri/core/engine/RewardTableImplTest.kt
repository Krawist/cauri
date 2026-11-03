package com.cauri.core.engine

import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

internal class RewardTableImplTest {
    private val rewards = RewardTableImpl()

    @Nested
    inner class Reward {
        @ParameterizedTest
        @EnumSource(ReminderAction::class)
        fun `should reward every outcome of every action between -1 and 1`(action: ReminderAction) {
            // when & then
            ReminderOutcome.entries.forEach { rewards.reward(action, it).shouldBeBetween(-1.0, 1.0, 0.0) }
        }

        @ParameterizedTest
        @CsvSource("NOTIFY_NOW, CONFIRMED, 1.0", "SKIP, MISSED, -1.0", "SKIP, NOTHING_SPENT, 0.5")
        fun `should reward a decision by its outcome`(
            action: ReminderAction,
            outcome: ReminderOutcome,
            expected: Double,
        ) {
            // when
            val reward = rewards.reward(action, outcome)

            // then
            reward shouldBe expected
        }

        @Test
        fun `should reward a confirmation more after a notification than after the summary`() {
            // when
            val notified = rewards.reward(ReminderAction.NOTIFY_NOW, ReminderOutcome.CONFIRMED)
            val summed = rewards.reward(ReminderAction.EVENING_SUMMARY, ReminderOutcome.CONFIRMED)

            // then
            (notified > summed) shouldBe true
        }
    }
}
