package com.cauri.core.engine

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ReminderGuardrailsImplTest {
    private val guardrails = ReminderGuardrailsImpl()

    @Nested
    inner class Allowed {
        @Test
        fun `should allow every action by day, under the cap, away from home`() {
            // when
            val allowed = guardrails.allowed(context(), limits())

            // then
            allowed shouldContainExactlyInAnyOrder ReminderAction.entries
        }

        @ParameterizedTest
        @CsvSource("22, 22, 7", "3, 22, 7", "6, 22, 7", "13, 12, 14")
        fun `should never notify in the quiet hours`(hour: Int, from: Int, until: Int) {
            // when
            val allowed = guardrails.allowed(context(hour = hour), limits(quietFrom = from, quietUntil = until))

            // then
            allowed shouldContainExactlyInAnyOrder listOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP)
        }

        @ParameterizedTest
        @CsvSource("7, 22, 7", "21, 22, 7", "14, 12, 14", "3, 0, 0")
        fun `should notify outside the quiet hours`(hour: Int, from: Int, until: Int) {
            // when
            val allowed = guardrails.allowed(context(hour = hour), limits(quietFrom = from, quietUntil = until))

            // then
            allowed shouldContainExactlyInAnyOrder ReminderAction.entries
        }

        @Test
        fun `should stop notifying once the daily cap is reached`() {
            // when
            val allowed = guardrails.allowed(context().copy(notificationsToday = 4), limits(dailyCap = 4))

            // then
            allowed shouldContainExactlyInAnyOrder listOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP)
        }

        @Test
        fun `should leave 30 minutes between two notifications`() {
            // when
            val allowed = guardrails.allowed(context().copy(minutesSinceNotification = 12), limits())

            // then
            allowed shouldContainExactlyInAnyOrder listOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP)
        }

        @Test
        fun `should never notify at home or at work`() {
            // when
            val allowed = guardrails.allowed(context().copy(atHomeOrWork = true), limits())

            // then
            allowed shouldContainExactlyInAnyOrder listOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP)
        }

        @Test
        fun `should skip a visit when the user turned the visit reminders off`() {
            // when
            val allowed = guardrails.allowed(
                context().copy(trigger = ReminderTrigger.VISIT_END),
                limits(visitReminders = false),
            )

            // then
            allowed shouldContainExactlyInAnyOrder listOf(ReminderAction.SKIP)
        }

        @Test
        fun `should still remind an operation read in a message with the visit reminders off`() {
            // when
            val allowed = guardrails.allowed(context(), limits(visitReminders = false))

            // then
            allowed shouldContainExactlyInAnyOrder ReminderAction.entries
        }
    }

    @Nested
    inner class Restrict {
        @Test
        fun `should keep an allowed action`() {
            // when
            val action = guardrails.restrict(ReminderAction.NOTIFY_NOW, ReminderAction.entries.toSet())

            // then
            action shouldBe ReminderAction.NOTIFY_NOW
        }

        @Test
        fun `should turn a notification into a line of the evening summary`() {
            // when
            val action = guardrails.restrict(
                ReminderAction.NOTIFY_NOW,
                setOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP),
            )

            // then
            action shouldBe ReminderAction.EVENING_SUMMARY
        }

        @Test
        fun `should skip when only skipping is allowed`() {
            // when
            val action = guardrails.restrict(ReminderAction.EVENING_SUMMARY, setOf(ReminderAction.SKIP))

            // then
            action shouldBe ReminderAction.SKIP
        }
    }

    private fun limits(quietFrom: Int = 22, quietUntil: Int = 7, dailyCap: Int = 4, visitReminders: Boolean = true) =
        ReminderLimits(
            quietFrom = quietFrom,
            quietUntil = quietUntil,
            dailyCap = dailyCap,
            visitReminders = visitReminders,
        )

    private fun context(hour: Int = 11) = ReminderContext(
        trigger = ReminderTrigger.DETECTED_OPERATION,
        at = ZonedDateTime.of(2026, 11, 3, hour, 15, 0, 0, ZoneId.of("Africa/Douala")),
        expense = true,
        stayMinutes = null,
        placeVisits = 0,
        placeSpendRate = null,
        minutesSinceTransaction = null,
        minutesSinceNotification = null,
        notificationsToday = 0,
        cashBalance = null,
        atHomeOrWork = false,
    )
}
