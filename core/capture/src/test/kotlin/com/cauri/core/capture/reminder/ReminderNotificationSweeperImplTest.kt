package com.cauri.core.capture.reminder

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.repository.FakeTransactionRepository
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReminderNotificationSweeperImplTest {
    private val transactions = FakeTransactionRepository()
    private val notifier = KeepingNotifier()
    private val sweeper = ReminderNotificationSweeperImpl(transactions, notifier, UnconfinedTestDispatcher())

    @Nested
    inner class Start {
        @Test
        fun `should keep only the notifications of the cards still waiting`() {
            // given
            transactions.pending.value = listOf(card(1), card(2))
            sweeper.start()

            // when
            transactions.pending.value = listOf(card(2))

            // then
            notifier.kept shouldContainExactly listOf(setOf(1L, 2L), setOf(2L))
        }

        @Test
        fun `should follow the cards once, however often it is started`() {
            // given
            sweeper.start()

            // when
            sweeper.start()

            // then
            notifier.kept shouldContainExactly listOf(emptySet())
        }
    }

    private fun card(id: Long) = PendingOperation(
        transactionId = id,
        accountId = 2,
        type = PendingType.EXPENSE,
        amount = 1_000,
        fees = 0,
        foreignAmount = null,
        foreignCurrency = null,
        accountName = "Orange Money",
        accountKind = AccountKind.MOBILE_MONEY,
        toAccountName = null,
        toAccountKind = null,
        counterparty = null,
        occurredAt = Instant.parse("2026-11-04T10:00:00Z"),
        categoryId = null,
    )

    // Keeps what the sweeper asked to keep, each time.
    private class KeepingNotifier : ReminderNotifier {
        val kept = mutableListOf<Set<Long>>()

        override suspend fun show(operation: PendingOperation) = true

        override suspend fun showSummary(count: Int) = true

        override fun dismiss(transactionId: Long) = Unit

        override fun keepOnly(transactionIds: Set<Long>) {
            kept += transactionIds
        }
    }
}
