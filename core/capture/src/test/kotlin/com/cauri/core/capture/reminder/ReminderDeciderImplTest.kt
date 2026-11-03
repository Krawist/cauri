package com.cauri.core.capture.reminder

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.ReminderAction
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.VisitHistory
import com.cauri.core.data.repository.FakeCashCountRepository
import com.cauri.core.data.repository.FakeDecisionLogRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import com.cauri.core.engine.FeatureEncoder
import com.cauri.core.engine.FeatureEncoderImpl
import com.cauri.core.engine.ReminderRules
import com.cauri.core.engine.ReminderRulesImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ReminderDeciderImplTest {
    private val transactions = FakeTransactionRepository()
    private val log = FakeDecisionLogRepository()
    private val notifier = RecordingNotifier()
    private val decider = ReminderDeciderImpl(
        transactions,
        log,
        FakeCashCountRepository(),
        ReminderRulesImpl(),
        FeatureEncoderImpl(),
        notifier,
        Clock.fixed(NOW, ZoneId.of("Africa/Douala")),
    )

    @Nested
    inner class DecidePending {
        @Test
        fun `should notify an expense read and log the decision of the rules with its context`() = runTest {
            // given
            transactions.pending.value = listOf(payment(id = 7))

            // when
            decider.decidePending()

            // then
            notifier.shown shouldContainExactly listOf(7L)
            log.decisions.single().let {
                listOf(it.action, it.probability, it.modelVersion, it.featureVersion, it.features.size)
            } shouldBe
                listOf(
                    ReminderAction.NOTIFY_NOW,
                    1.0,
                    ReminderRules.VERSION,
                    FeatureEncoder.VERSION,
                    FeatureEncoder.SIZE,
                )
        }

        @Test
        fun `should decide about a card once`() = runTest {
            // given
            transactions.pending.value = listOf(payment(id = 7))
            decider.decidePending()

            // when
            decider.decidePending()

            // then
            log.decisions shouldHaveSize 1
        }

        @Test
        fun `should leave out a card older than two hours`() = runTest {
            // given
            transactions.pending.value = listOf(payment(id = 7, at = NOW - Duration.ofHours(3)))

            // when
            decider.decidePending()

            // then
            log.decisions.shouldBeEmpty()
        }

        @Test
        fun `should keep a second card for the evening, half an hour after the first notification`() = runTest {
            // given
            transactions.pending.value = listOf(payment(id = 7), payment(id = 8, at = NOW - Duration.ofMinutes(1)))

            // when
            decider.decidePending()

            // then
            log.decisions.map { it.transactionId to it.action } shouldContainExactly listOf(
                7L to ReminderAction.NOTIFY_NOW,
                8L to ReminderAction.EVENING_SUMMARY,
            )
        }

        @Test
        fun `should leave alone a place that rarely leads to an expense`() = runTest {
            // given
            transactions.pending.value = listOf(payment(id = 7).copy(stayMinutes = 25))
            log.histories[7] = VisitHistory(visitId = 3, placeVisits = 10, visitsWithExpense = 1)

            // when
            decider.decidePending()

            // then
            log.decisions.single().let { it.action to it.visitId } shouldBe (ReminderAction.SKIP to 3L)
            notifier.shown.shouldBeEmpty()
        }

        @Test
        fun `should keep for the evening a card come soon after a notification`() = runTest {
            // given
            log.stats =
                ReminderStats(
                    notificationsToday = 1,
                    lastNotifiedAt = NOW - Duration.ofMinutes(10),
                    lastTransactionAt = null,
                )
            transactions.pending.value = listOf(payment(id = 7))

            // when
            decider.decidePending()

            // then
            log.decisions.single().action shouldBe ReminderAction.EVENING_SUMMARY
        }
    }

    private fun payment(id: Long, at: Instant = NOW - Duration.ofMinutes(5)) = PendingOperation(
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
        occurredAt = at,
    )

    private class RecordingNotifier : ReminderNotifier {
        val shown = mutableListOf<Long>()

        override suspend fun show(operation: PendingOperation): Boolean {
            shown += operation.transactionId
            return true
        }

        override fun dismiss(transactionId: Long) = Unit
    }

    private companion object {
        // 11 h in Douala: no quiet hour.
        val NOW: Instant = Instant.parse("2026-11-03T10:00:00Z")
    }
}
