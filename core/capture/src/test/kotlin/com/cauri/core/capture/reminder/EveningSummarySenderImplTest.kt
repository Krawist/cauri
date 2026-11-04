package com.cauri.core.capture.reminder

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeDecisionLogRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import com.cauri.core.engine.SummaryTimeImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class EveningSummarySenderImplTest {
    private val transactions = FakeTransactionRepository()
    private val log = FakeDecisionLogRepository()
    private val preferences = FakeUserPreferencesRepository()
    private val notifier = RecordingNotifier()

    @Nested
    inner class SendIfDue {
        @Test
        fun `should send the summary of the cards waiting at its time, once a day`() = runTest {
            // given
            transactions.pending.value = listOf(card(id = 7), card(id = 8))

            // when
            sender(at = "20:45").sendIfDue()

            // then
            notifier.summaries shouldContainExactly listOf(2)
            preferences.lastEveningSummary.value shouldBe DAY
        }

        @Test
        fun `should wait for its time`() = runTest {
            // given
            transactions.pending.value = listOf(card(id = 7))

            // when
            sender(at = "20:15").sendIfDue()

            // then
            notifier.summaries.shouldBeEmpty()
        }

        @Test
        fun `should send nothing when no card waits`() = runTest {
            // when
            sender(at = "20:45").sendIfDue()

            // then
            notifier.summaries.shouldBeEmpty()
            preferences.lastEveningSummary.value shouldBe null
        }

        @Test
        fun `should send nothing a second time the same day`() = runTest {
            // given
            transactions.pending.value = listOf(card(id = 7))
            preferences.lastEveningSummary.value = DAY

            // when
            sender(at = "21:30").sendIfDue()

            // then
            notifier.summaries.shouldBeEmpty()
        }

        @Test
        fun `should keep the cards for tomorrow from 23 h`() = runTest {
            // given
            transactions.pending.value = listOf(card(id = 7))

            // when
            sender(at = "23:10").sendIfDue()

            // then
            notifier.summaries.shouldBeEmpty()
        }

        @Test
        fun `should come at the time the user chose`() = runTest {
            // given
            transactions.pending.value = listOf(card(id = 7))
            preferences.reminders.value = preferences.reminders.value.copy(summaryTime = LocalTime.of(19, 0))

            // when
            sender(at = "19:05").sendIfDue()

            // then
            notifier.summaries shouldContainExactly listOf(1)
        }

        @Test
        fun `should follow when the user usually answers`() = runTest {
            // given
            transactions.pending.value = listOf(card(id = 7))
            log.answers = List(10) { LocalDate.of(2026, 11, 1).atTime(19, 10).atZone(ZONE).toInstant() }

            // when
            sender(at = "19:05").sendIfDue()

            // then
            notifier.summaries shouldContainExactly listOf(1)
        }
    }

    private fun sender(at: String) = EveningSummarySenderImpl(
        transactions = transactions,
        log = log,
        preferences = preferences,
        summaryTime = SummaryTimeImpl(),
        notifier = notifier,
        clock = Clock.fixed(DAY.atTime(LocalTime.parse(at)).atZone(ZONE).toInstant(), ZONE),
    )

    private fun card(id: Long) = PendingOperation(
        transactionId = id,
        accountId = 2,
        type = PendingType.EXPENSE,
        amount = 5_000,
        fees = 0,
        foreignAmount = null,
        foreignCurrency = null,
        accountName = "Orange Money",
        accountKind = AccountKind.MOBILE_MONEY,
        toAccountName = null,
        toAccountKind = null,
        counterparty = "BOULANGERIE SAKER",
        occurredAt = Instant.EPOCH,
    )

    private class RecordingNotifier : ReminderNotifier {
        val summaries = mutableListOf<Int>()

        override suspend fun show(operation: PendingOperation) = true

        override suspend fun showSummary(count: Int): Boolean {
            summaries += count
            return true
        }

        override fun dismiss(transactionId: Long) = Unit

        override fun keepOnly(transactionIds: Set<Long>) = Unit
    }

    private companion object {
        val ZONE: ZoneId = ZoneId.of("Africa/Douala")
        val DAY: LocalDate = LocalDate.of(2026, 11, 3)
    }
}
