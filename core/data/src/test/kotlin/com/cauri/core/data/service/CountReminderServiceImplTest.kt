package com.cauri.core.data.service

import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderReason
import com.cauri.core.data.model.CountReminderSettings
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CountReminderServiceImplTest {
    private val service = CountReminderServiceImpl()

    @Nested
    inner class Reason {
        @Test
        fun `should remind after a withdrawal today`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20),
                now = at(day = 3, hour = 20),
                balance = countedOn(day = 2, expenses = 0),
                withdrawalToday = true,
                lastReminder = null,
            )

            // then
            reason shouldBe CountReminderReason.WITHDRAWAL
        }

        @Test
        fun `should remind after several cash expenses since the count`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20),
                now = at(day = 3, hour = 20),
                balance = countedOn(day = 2, expenses = 3),
                withdrawalToday = false,
                lastReminder = null,
            )

            // then
            reason shouldBe CountReminderReason.EXPENSES
        }

        @Test
        fun `should remind when the count is three days old`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20),
                now = at(day = 5, hour = 20),
                balance = countedOn(day = 2, expenses = 0),
                withdrawalToday = false,
                lastReminder = null,
            )

            // then
            reason shouldBe CountReminderReason.DAYS_WITHOUT_COUNT
        }

        @Test
        fun `should not remind when nothing makes it useful`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20),
                now = at(day = 3, hour = 20),
                balance = countedOn(day = 2, expenses = 2),
                withdrawalToday = false,
                lastReminder = null,
            )

            // then
            reason.shouldBeNull()
        }

        @Test
        fun `should remind every evening when the user asked for it`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.EVERY_EVENING, hour = 20),
                now = at(day = 3, hour = 20),
                balance = countedOn(day = 2, expenses = 0),
                withdrawalToday = false,
                lastReminder = null,
            )

            // then
            reason shouldBe CountReminderReason.EVENING
        }

        @ParameterizedTest
        @CsvSource("19", "23")
        fun `should only remind between the chosen hour and the end of the evening`(hour: Int) {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.EVERY_EVENING, hour = 20),
                now = at(day = 3, hour = hour),
                balance = countedOn(day = 2, expenses = 0),
                withdrawalToday = true,
                lastReminder = null,
            )

            // then
            reason.shouldBeNull()
        }

        @Test
        fun `should send no more than one reminder a day`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.EVERY_EVENING, hour = 20),
                now = at(day = 3, hour = 21),
                balance = countedOn(day = 2, expenses = 0),
                withdrawalToday = true,
                lastReminder = LocalDate.of(2026, 11, 3),
            )

            // then
            reason.shouldBeNull()
        }

        @Test
        fun `should not remind when the cash was counted today`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.EVERY_EVENING, hour = 20),
                now = at(day = 3, hour = 20),
                balance = countedOn(day = 3, expenses = 0),
                withdrawalToday = true,
                lastReminder = null,
            )

            // then
            reason.shouldBeNull()
        }

        @Test
        fun `should never remind when the user turned the reminders off`() {
            // when
            val reason = service.reason(
                settings = CountReminderSettings(frequency = CountReminderFrequency.NEVER, hour = 20),
                now = at(day = 3, hour = 20),
                balance = countedOn(day = 2, expenses = 5),
                withdrawalToday = true,
                lastReminder = null,
            )

            // then
            reason.shouldBeNull()
        }
    }

    // A moment of November 2026, in UTC.
    private fun at(day: Int, hour: Int) = ZonedDateTime.of(2026, 11, day, hour, 0, 0, 0, ZoneOffset.UTC)

    // The balance of a count made at 8:00 on that day of November 2026.
    private fun countedOn(day: Int, expenses: Int) =
        CashBalance(amount = 7_500, countedAt = at(day = day, hour = 8).toInstant(), expensesSinceCount = expenses)
}
