package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.service.DuplicateServiceImpl
import com.cauri.core.data.service.TransferLinkServiceImpl
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TransferRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var transactions: TransactionRepositoryImpl
    private lateinit var repository: TransferRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        transactions = TransactionRepositoryImpl(
            database.transactionDao(),
            database.transactionMergeDao(),
            database.accountDao(),
            DuplicateServiceImpl(),
            CLOCK,
        )
        repository = TransferRepositoryImpl(database.transferLinkDao(), TransferLinkServiceImpl(), CLOCK)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class LinkTransfers {
        @Test
        fun `should turn a debit and a credit into one transfer with the fee kept on the way`() = runTest {
            // given
            val ubaId = database.accountDao().insert(account(name = "UBA", provider = "UBA"))
            val orangeId = database.accountDao().insert(account(name = "Orange Money", provider = "ORANGE_MONEY"))
            detect(accountId = ubaId, type = DetectedType.EXPENSE, amount = 100_000)
            detect(accountId = orangeId, type = DetectedType.INCOME, amount = 99_000)

            // when
            val linked = repository.linkTransfers()

            // then
            linked shouldBe 1
            recorded().map { Triple(it.accountId, it.type, it.amount) } shouldContainExactlyInAnyOrder listOf(
                Triple(ubaId, TransactionType.TRANSFER_OUT, 99_000L),
                Triple(orangeId, TransactionType.TRANSFER_IN, 99_000L),
                Triple(ubaId, TransactionType.EXPENSE, 1_000L),
            )
        }

        @Test
        fun `should link nothing twice`() = runTest {
            // given
            val ubaId = database.accountDao().insert(account(name = "UBA", provider = "UBA"))
            val orangeId = database.accountDao().insert(account(name = "Orange Money", provider = "ORANGE_MONEY"))
            detect(accountId = ubaId, type = DetectedType.EXPENSE, amount = 10_000)
            detect(accountId = orangeId, type = DetectedType.INCOME, amount = 10_000)
            repository.linkTransfers()

            // when
            val linked = repository.linkTransfers()

            // then
            linked shouldBe 0
        }
    }

    @Nested
    inner class UnlinkTransfer {
        @Test
        fun `should count the debit and the credit apart again, and never link them again`() = runTest {
            // given
            val ubaId = database.accountDao().insert(account(name = "UBA", provider = "UBA"))
            val orangeId = database.accountDao().insert(account(name = "Orange Money", provider = "ORANGE_MONEY"))
            detect(accountId = ubaId, type = DetectedType.EXPENSE, amount = 100_000)
            detect(accountId = orangeId, type = DetectedType.INCOME, amount = 99_000)
            repository.linkTransfers()

            // when
            repository.unlinkTransfer(1)
            val linkedAgain = repository.linkTransfers()

            // then
            recorded().map { Triple(it.accountId, it.type, it.amount) } shouldContainExactlyInAnyOrder listOf(
                Triple(ubaId, TransactionType.EXPENSE, 100_000L),
                Triple(orangeId, TransactionType.INCOME, 99_000L),
            )
            linkedAgain shouldBe 0
        }
    }

    @Nested
    inner class Pending {
        @Test
        fun `should show a linked transfer as one operation between the two accounts`() = runTest {
            // given
            val ubaId = database.accountDao().insert(account(name = "UBA", provider = "UBA"))
            val orangeId = database.accountDao().insert(account(name = "Orange Money", provider = "ORANGE_MONEY"))
            detect(accountId = ubaId, type = DetectedType.EXPENSE, amount = 10_000, counterparty = "ORANGEMONEYCM")
            detect(accountId = orangeId, type = DetectedType.INCOME, amount = 10_000, counterparty = "UBA")
            repository.linkTransfers()

            // when
            val pending = transactions.pending.first()

            // then
            pending.map { listOf(it.type, it.accountName, it.toAccountName, it.counterparty) } shouldContainExactly
                listOf(listOf(PendingType.TRANSFER, "UBA", "Orange Money", null))
        }
    }

    private suspend fun detect(accountId: Long, type: DetectedType, amount: Long, counterparty: String? = null) {
        val signalId = database.rawSignalDao().insert(
            RawSignalEntity(
                sender = "com.samsung.android.messaging",
                title = "OrangeMoney",
                text = "Transfert",
                receivedAt = Instant.EPOCH,
                templateVersion = null,
            ),
        )
        transactions.recordDetected(
            DetectedOperation(
                rawSignalId = signalId,
                templateVersion = "test@1",
                accountId = accountId,
                type = type,
                amount = amount,
                fee = null,
                foreignAmount = null,
                foreignCurrency = null,
                balance = null,
                counterparty = counterparty,
                occurredAt = Instant.ofEpochSecond(1_000),
            ),
        )
    }

    private suspend fun recorded(): List<TransactionEntity> =
        TransactionStatus.entries.flatMap { database.transactionDao().observeByStatus(it).first() }

    private fun account(name: String, provider: String) = AccountEntity(
        name = name,
        kind = AccountKind.MOBILE_MONEY,
        provider = provider,
        currency = "XAF",
        status = AccountStatus.ACTIVE,
    )

    private companion object {
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
