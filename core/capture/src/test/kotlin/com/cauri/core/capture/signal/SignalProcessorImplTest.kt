package com.cauri.core.capture.signal

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import com.cauri.core.data.repository.FakeAccountRepository
import com.cauri.core.data.repository.FakeRawSignalRepository
import com.cauri.core.data.repository.FakeTransactionRepository
import com.cauri.core.data.repository.FakeTransferRepository
import com.cauri.core.data.repository.FakeVisitRepository
import com.cauri.core.parsing.MessageParserImpl
import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.TemplateCatalog
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SignalProcessorImplTest {
    private val rawSignals = FakeRawSignalRepository()
    private val accounts = FakeAccountRepository()
    private val transactions = FakeTransactionRepository()
    private val transfers = FakeTransferRepository()
    private val preferences = FakeUserPreferencesRepository()
    private val visits = FakeVisitRepository()

    // A made-up template: the processing, not the operators' formats, is tested here.
    private val processor = SignalProcessorImpl(
        rawSignalRepository = rawSignals,
        templateCatalogRepository = object : TemplateCatalogRepository {
            override val catalog = TemplateCatalog(
                version = "test",
                templates = listOf(
                    MessageTemplate(
                        id = "orange-money.payment",
                        version = 1,
                        provider = "ORANGE_MONEY",
                        senders = listOf("OrangeMoney"),
                        kind = MessageKind.PAYMENT,
                        direction = Direction.DEBIT,
                        currency = "XAF",
                        pattern = "Paiement de (?<amount>\\d+) FCFA",
                    ),
                ),
            )
        },
        messageParser = MessageParserImpl(),
        detectionService = DetectionServiceImpl(),
        accountRepository = accounts,
        transactionRepository = transactions,
        transferRepository = transfers,
        preferencesRepository = preferences,
        visitRepository = visits,
        clock = Clock.systemUTC(),
    )

    @Nested
    inner class Process {
        @Test
        fun `should record what a signal reports on the account of its provider`() = runTest {
            // given
            val accountId = accounts.createAccount(orangeMoney())
            payment(text = "Paiement de 2500 FCFA")

            // when
            processor.process()

            // then
            transactions.detected.map { Triple(it.accountId, it.type, it.amount) } shouldContainExactly
                listOf(Triple(accountId, DetectedType.EXPENSE, 2_500L))
        }

        @Test
        fun `should offer to follow an account it recognises messages of`() = runTest {
            // given
            payment(text = "Paiement de 2500 FCFA")

            // when
            processor.process()

            // then
            processor.proposedAccounts.first() shouldContainExactly listOf(orangeMoney())
            transactions.detected.shouldBeEmpty()
        }

        @Test
        fun `should not offer again an account the user ignored`() = runTest {
            // given
            processor.ignore("ORANGE_MONEY")
            payment(text = "Paiement de 2500 FCFA")

            // when
            processor.process()

            // then
            processor.proposedAccounts.first().shouldBeEmpty()
        }

        @Test
        fun `should link the transfers once the signals are read`() = runTest {
            // given
            accounts.createAccount(orangeMoney())
            payment(text = "Paiement de 2500 FCFA")

            // when
            processor.process()

            // then
            transfers.linkRuns shouldBe 1
        }

        @Test
        fun `should leave a message no template recognises`() = runTest {
            // given
            accounts.createAccount(orangeMoney())
            payment(text = "Votre code est 1234")

            // when
            processor.process()

            // then
            transactions.detected.shouldBeEmpty()
            processor.proposedAccounts.first().shouldBeEmpty()
        }
    }

    @Nested
    inner class Follow {
        @Test
        fun `should create the account and read its messages received so far`() = runTest {
            // given
            payment(text = "Paiement de 2500 FCFA")
            processor.process()

            // when
            processor.follow("ORANGE_MONEY")

            // then
            accounts.created shouldContainExactly listOf(orangeMoney())
            transactions.detected.map { it.amount } shouldContainExactly listOf(2_500L)
            processor.proposedAccounts.first().shouldBeEmpty()
        }

        @Test
        fun `should let the user follow an account they ignored`() = runTest {
            // given
            processor.ignore("ORANGE_MONEY")

            // when
            processor.follow("ORANGE_MONEY")

            // then
            processor.ignoredAccounts.first().shouldBeEmpty()
            accounts.created shouldContainExactly listOf(orangeMoney())
        }
    }

    @Nested
    inner class Ignore {
        @Test
        fun `should stop offering the account`() = runTest {
            // given
            payment(text = "Paiement de 2500 FCFA")
            processor.process()

            // when
            processor.ignore("ORANGE_MONEY")

            // then
            processor.proposedAccounts.first().shouldBeEmpty()
            processor.ignoredAccounts.first() shouldBe listOf(orangeMoney())
        }
    }

    private suspend fun payment(text: String) {
        rawSignals.record(
            sender = "com.samsung.android.messaging",
            title = "OrangeMoney",
            text = text,
            receivedAt = Instant.ofEpochSecond(1_000),
        )
    }

    private fun orangeMoney() =
        NewAccount(name = "Orange Money", kind = AccountKind.MOBILE_MONEY, provider = "ORANGE_MONEY", currency = "XAF")
}
