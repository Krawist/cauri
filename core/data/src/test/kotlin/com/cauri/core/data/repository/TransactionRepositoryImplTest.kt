package com.cauri.core.data.repository

import android.database.SQLException
import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.Withdrawal
import com.cauri.core.data.service.CashBalanceServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TransactionRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: TransactionRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = TransactionRepositoryImpl(database.transactionDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class RecordWithdrawal {
        @Test
        fun `should record a transfer from the account to the Cash account`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())

            // when
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = null,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    rawSignalId = null,
                ),
            )

            // then
            val sides = recorded()
            val group = sides.first().transferGroup.shouldNotBeNull()
            sides.map { it.copy(id = 0) } shouldContainExactlyInAnyOrder listOf(
                manualTransfer(accountId = momoId, type = TransactionType.TRANSFER_OUT, group = group),
                manualTransfer(accountId = CASH_ACCOUNT_ID, type = TransactionType.TRANSFER_IN, group = group),
            )
        }

        @Test
        fun `should record the fee as a separate expense on the account`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())

            // when
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = 300,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    rawSignalId = null,
                ),
            )

            // then
            val expenses = recorded().filter { it.type == TransactionType.EXPENSE }
            expenses.map { it.copy(id = 0) } shouldContainExactlyInAnyOrder listOf(
                TransactionEntity(
                    accountId = momoId,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.CONFIRMED,
                    origin = TransactionOrigin.MANUAL,
                    amount = 300,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    categoryId = null,
                    placeId = null,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = null,
                    note = null,
                ),
            )
        }

        @Test
        fun `should wait for confirmation and keep the signal on the account's side when parsed`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(withdrawalSignal())

            // when
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = null,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    rawSignalId = signalId,
                ),
            )

            // then
            recorded().map { Triple(it.type, it.status, it.rawSignalId) } shouldContainExactlyInAnyOrder listOf(
                Triple(TransactionType.TRANSFER_OUT, TransactionStatus.DETECTED, signalId),
                Triple(TransactionType.TRANSFER_IN, TransactionStatus.DETECTED, null),
            )
        }

        @Test
        fun `should record nothing when the signal already created a transaction`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(withdrawalSignal())
            val withdrawal = Withdrawal(
                accountId = momoId,
                amount = 20_000,
                fee = 300,
                occurredAt = Instant.ofEpochSecond(1_000),
                rawSignalId = signalId,
            )
            repository.recordWithdrawal(withdrawal)

            // when
            shouldThrow<SQLException> { repository.recordWithdrawal(withdrawal) }

            // then
            recorded() shouldHaveSize 3
        }

        @Test
        fun `should raise the cash balance by the amount withdrawn`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val cashCounts = CashCountRepositoryImpl(
                cashCountDao = database.cashCountDao(),
                transactionDao = database.transactionDao(),
                cashBalanceService = CashBalanceServiceImpl(),
            )
            cashCounts.addCount(amount = 7_500, countedAt = Instant.ofEpochSecond(500), expectedAmount = null)

            // when
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = 300,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    rawSignalId = null,
                ),
            )

            // then
            cashCounts.balance.first().shouldNotBeNull().amount shouldBe 27_500
        }
    }

    @Nested
    inner class RecordCashExpense {
        @Test
        fun `should record a confirmed expense on the Cash account`() = runTest {
            // when
            val id = repository.recordCashExpense(
                amount = 500,
                categoryId = null,
                note = "Moto",
                occurredAt = Instant.ofEpochSecond(1_000),
            )

            // then
            recorded() shouldContainExactlyInAnyOrder listOf(
                TransactionEntity(
                    id = id,
                    accountId = CASH_ACCOUNT_ID,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.CONFIRMED,
                    origin = TransactionOrigin.MANUAL,
                    amount = 500,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    categoryId = null,
                    placeId = null,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = null,
                    note = "Moto",
                ),
            )
        }
    }

    @Nested
    inner class DeleteTransaction {
        @Test
        fun `should remove the transaction`() = runTest {
            // given
            val id = repository.recordCashExpense(
                amount = 500,
                categoryId = null,
                note = "Moto",
                occurredAt = Instant.ofEpochSecond(1_000),
            )

            // when
            repository.deleteTransaction(id)

            // then
            recorded() shouldHaveSize 0
        }
    }

    private suspend fun recorded(): List<TransactionEntity> =
        TransactionStatus.entries.flatMap { database.transactionDao().observeByStatus(it).first() }

    private fun momo() = AccountEntity(
        name = "MTN MoMo",
        kind = AccountKind.MOBILE_MONEY,
        provider = "MTN_MOMO",
        currency = "XAF",
        status = AccountStatus.ACTIVE,
    )

    private fun withdrawalSignal() = RawSignalEntity(
        sender = "com.mtn.momo",
        text = "Retrait de 20000 FCFA",
        receivedAt = Instant.EPOCH,
        templateVersion = "momo-1",
    )

    // One side of the manual withdrawal of 20 000 made at second 1 000.
    private fun manualTransfer(accountId: Long, type: TransactionType, group: String) = TransactionEntity(
        accountId = accountId,
        type = type,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = 20_000,
        occurredAt = Instant.ofEpochSecond(1_000),
        categoryId = null,
        placeId = null,
        rawSignalId = null,
        transferGroup = group,
        counterparty = null,
        note = null,
    )
}
