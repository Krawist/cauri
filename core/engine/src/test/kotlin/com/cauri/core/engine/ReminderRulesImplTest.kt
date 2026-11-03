package com.cauri.core.engine

import io.kotest.matchers.shouldBe
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ReminderRulesImplTest {
    private val rules = ReminderRulesImpl()

    @Nested
    inner class Decide {
        @ParameterizedTest
        @CsvSource(
            "25, 0, , NOTIFY_NOW",
            "25, 6, 0.6, NOTIFY_NOW",
            "6, 0, , EVENING_SUMMARY",
            "25, 6, 0.3, EVENING_SUMMARY",
            "25, 6, 0.1, SKIP",
            "6, 6, 0.1, SKIP",
            "25, 3, 0.1, NOTIFY_NOW",
        )
        fun `should decide a visit by its stay and how often the place leads to an expense`(
            stay: Long,
            visits: Int,
            rate: Double?,
            expected: ReminderAction,
        ) {
            // when
            val action = rules.decide(visit(stay = stay, visits = visits, rate = rate))

            // then
            action shouldBe expected
        }

        @Test
        fun `should notify an expense read in a message`() {
            // when
            val action = rules.decide(context(ReminderTrigger.DETECTED_OPERATION, expense = true))

            // then
            action shouldBe ReminderAction.NOTIFY_NOW
        }

        @Test
        fun `should keep an income for the evening summary`() {
            // when
            val action = rules.decide(context(ReminderTrigger.DETECTED_OPERATION, expense = false))

            // then
            action shouldBe ReminderAction.EVENING_SUMMARY
        }

        @Test
        fun `should keep a withdrawal for the evening summary`() {
            // when
            val action = rules.decide(context(ReminderTrigger.WITHDRAWAL, expense = false))

            // then
            action shouldBe ReminderAction.EVENING_SUMMARY
        }

        @ParameterizedTest
        @CsvSource("22", "23", "3", "6")
        fun `should never notify at night`(hour: Int) {
            // when
            val action = rules.decide(context(ReminderTrigger.DETECTED_OPERATION, expense = true, hour = hour))

            // then
            action shouldBe ReminderAction.EVENING_SUMMARY
        }

        @Test
        fun `should leave 30 minutes between two notifications`() {
            // when
            val action = rules.decide(
                context(ReminderTrigger.DETECTED_OPERATION, expense = true).copy(minutesSinceNotification = 12),
            )

            // then
            action shouldBe ReminderAction.EVENING_SUMMARY
        }
    }

    @Nested
    inner class Score {
        @Test
        fun `should score a notification above the summary, and the summary above nothing`() {
            // then
            listOf(
                ReminderAction.NOTIFY_NOW,
                ReminderAction.EVENING_SUMMARY,
                ReminderAction.SKIP,
            ).map(rules::score) shouldBe
                listOf(1.0, 0.5, 0.0)
        }
    }

    private fun visit(stay: Long, visits: Int, rate: Double?) = context(
        ReminderTrigger.VISIT_END,
        expense = true,
    ).copy(stayMinutes = stay, placeVisits = visits, placeSpendRate = rate)

    private fun context(trigger: ReminderTrigger, expense: Boolean, hour: Int = 11) = ReminderContext(
        trigger = trigger,
        at = ZonedDateTime.of(2026, 11, 3, hour, 15, 0, 0, ZoneId.of("Africa/Douala")),
        expense = expense,
        stayMinutes = null,
        placeVisits = 0,
        placeSpendRate = null,
        minutesSinceTransaction = null,
        minutesSinceNotification = null,
        notificationsToday = 0,
        cashBalance = null,
    )
}
