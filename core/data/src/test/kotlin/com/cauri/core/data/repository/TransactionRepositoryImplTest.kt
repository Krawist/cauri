package com.cauri.core.data.repository

import android.database.SQLException
import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountBalanceEntity
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
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.EntryType
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.model.Withdrawal
import com.cauri.core.data.service.CashBalanceServiceImpl
import com.cauri.core.data.service.DuplicateServiceImpl
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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

internal class TransactionRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: TransactionRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = TransactionRepositoryImpl(
            database.transactionDao(),
            database.transactionMergeDao(),
            database.accountDao(),
            DuplicateServiceImpl(),
            CLOCK,
        )
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
                    foreignAmount = null,
                    foreignCurrency = null,
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
    inner class RecordDetected {
        @Test
        fun `should record an expense waiting for confirmation and mark its signal read`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = null),
            )

            // then
            recorded().map { it.copy(id = 0) } shouldContainExactlyInAnyOrder listOf(
                TransactionEntity(
                    accountId = momoId,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.DETECTED,
                    origin = TransactionOrigin.SIGNAL,
                    amount = 2_500,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    categoryId = null,
                    placeId = null,
                    rawSignalId = signalId,
                    transferGroup = null,
                    counterparty = "Boulangerie",
                    note = null,
                    foreignAmount = null,
                    foreignCurrency = null,
                    proposedAmount = 2_500,
                ),
            )
            database.rawSignalDao().getUnparsed().shouldBeEmpty()
        }

        @Test
        fun `should record the fee as a separate expense on the account`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.INCOME, fee = 50),
            )

            // then
            recorded().map { Triple(it.type, it.amount, it.rawSignalId) } shouldContainExactlyInAnyOrder listOf(
                Triple(TransactionType.INCOME, 2_500L, signalId),
                Triple(TransactionType.EXPENSE, 50L, null),
            )
        }

        @Test
        fun `should record a withdrawal as a transfer to the Cash account`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.WITHDRAWAL, fee = null),
            )

            // then
            recorded().map { Triple(it.accountId, it.type, it.transferGroup != null) } shouldContainExactlyInAnyOrder
                listOf(
                    Triple(momoId, TransactionType.TRANSFER_OUT, true),
                    Triple(CASH_ACCOUNT_ID, TransactionType.TRANSFER_IN, true),
                )
        }

        @Test
        fun `should record a deposit as a transfer from the Cash account`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.DEPOSIT, fee = null),
            )

            // then
            recorded().map { Triple(it.accountId, it.type, it.transferGroup != null) } shouldContainExactlyInAnyOrder
                listOf(
                    Triple(momoId, TransactionType.TRANSFER_IN, true),
                    Triple(CASH_ACCOUNT_ID, TransactionType.TRANSFER_OUT, true),
                )
        }

        @Test
        fun `should keep the balance the message gave`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = null)
                    .copy(balance = 12_500),
            )

            // then
            database.accountBalanceDao().observeLatest().first().map { it.copy(id = 0) } shouldContainExactly listOf(
                AccountBalanceEntity(
                    accountId = momoId,
                    amount = 12_500,
                    at = Instant.ofEpochSecond(1_000),
                    rawSignalId = signalId,
                ),
            )
        }

        @Test
        fun `should record nothing and leave the signal unread when it already created a transaction`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = null,
                    occurredAt = Instant.ofEpochSecond(1_000),
                    rawSignalId = signalId,
                ),
            )

            // when
            shouldThrow<SQLException> {
                repository.recordDetected(
                    operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = 50),
                )
            }

            // then
            recorded() shouldHaveSize 2
            database.rawSignalDao().getUnparsed() shouldHaveSize 1
        }
    }

    @Nested
    inner class Pending {
        @Test
        fun `should list an operation read in a message, its fee riding with it`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = 50),
            )

            // when
            val pending = repository.pending.first()

            // then
            pending.map { it.copy(transactionId = 0) } shouldContainExactly listOf(
                PendingOperation(
                    transactionId = 0,
                    type = PendingType.EXPENSE,
                    amount = 2_500,
                    foreignAmount = null,
                    foreignCurrency = null,
                    accountName = "MTN MoMo",
                    toAccountName = null,
                    counterparty = "Boulangerie",
                    occurredAt = Instant.ofEpochSecond(1_000),
                ),
            )
        }

        @Test
        fun `should list a withdrawal once, from the account to Cash`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.WITHDRAWAL, fee = null),
            )

            // when
            val pending = repository.pending.first()

            // then
            pending.map { Triple(it.type, it.accountName, it.toAccountName) } shouldContainExactly listOf(
                Triple(PendingType.TRANSFER, "MTN MoMo", "Cash"),
            )
        }

        @Test
        fun `should list the newest operation first`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val first = database.rawSignalDao().insert(unparsedSignal())
            val second = database.rawSignalDao().insert(unparsedSignal().copy(text = "Paiement de 300 FCFA"))
            repository.recordDetected(
                operation(rawSignalId = first, accountId = momoId, type = DetectedType.EXPENSE, fee = null),
            )
            repository.recordDetected(
                operation(rawSignalId = second, accountId = momoId, type = DetectedType.INCOME, fee = null)
                    .copy(occurredAt = Instant.ofEpochSecond(2_000)),
            )

            // when
            val pending = repository.pending.first()

            // then
            pending.map { it.type } shouldContainExactly listOf(PendingType.INCOME, PendingType.EXPENSE)
        }

        @Test
        fun `should leave out what the user entered`() = runTest {
            // given
            repository.recordCashExpense(
                amount = 500,
                categoryId = null,
                note = "Moto",
                occurredAt = Instant.ofEpochSecond(1_000),
            )

            // when
            val pending = repository.pending.first()

            // then
            pending.shouldBeEmpty()
        }
    }

    @Nested
    inner class RecordEntry {
        @Test
        fun `should record a confirmed entry when Cauri detected nothing alike`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())

            // when
            val recorded = repository.recordEntry(entry(accountId = momoId))

            // then
            recorded.mergeId.shouldBeNull()
            recorded().map { Triple(it.id, it.status, it.origin) } shouldContainExactlyInAnyOrder
                listOf(Triple(recorded.transactionId, TransactionStatus.CONFIRMED, TransactionOrigin.MANUAL))
        }

        @Test
        fun `should keep the category Cauri proposed next to the one chosen`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())

            // when
            repository.recordEntry(entry(accountId = momoId).copy(proposedCategoryId = 7))

            // then
            recorded().single().proposedCategoryId shouldBe 7L
        }

        @Test
        fun `should merge the entry into the operation Cauri detected`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = null),
            )
            val detectedId = recorded().single().id

            // when
            val recorded = repository.recordEntry(entry(accountId = momoId))

            // then
            recorded.transactionId shouldBe detectedId
            recorded().map { Triple(it.status, it.origin, it.note) } shouldContainExactlyInAnyOrder listOf(
                Triple(TransactionStatus.CONFIRMED, TransactionOrigin.SIGNAL, "Pain"),
                Triple(TransactionStatus.MERGED, TransactionOrigin.MANUAL, "Pain"),
            )
        }
    }

    @Nested
    inner class RecordDetectedAfterEntry {
        @Test
        fun `should merge the operation into the entry the user typed before`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            repository.recordEntry(entry(accountId = momoId))

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = null),
            )

            // then
            recorded().map { Triple(it.status, it.rawSignalId, it.note) } shouldContainExactlyInAnyOrder listOf(
                Triple(TransactionStatus.CONFIRMED, signalId, "Pain"),
                Triple(TransactionStatus.MERGED, null, "Pain"),
            )
        }

        @Test
        fun `should keep the cash balance counting the operation once`() = runTest {
            // given
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            val cashCounts = CashCountRepositoryImpl(
                cashCountDao = database.cashCountDao(),
                transactionDao = database.transactionDao(),
                cashBalanceService = CashBalanceServiceImpl(),
            )
            cashCounts.addCount(amount = 10_000, countedAt = Instant.EPOCH, expectedAmount = null)
            repository.recordEntry(entry(accountId = CASH_ACCOUNT_ID))

            // when
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = CASH_ACCOUNT_ID, type = DetectedType.EXPENSE, fee = null),
            )

            // then
            cashCounts.balance.first().shouldNotBeNull().amount shouldBe 7_500
        }
    }

    @Nested
    inner class UndoMerge {
        @Test
        fun `should count the two transactions apart again`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            val signalId = database.rawSignalDao().insert(unparsedSignal())
            repository.recordDetected(
                operation(rawSignalId = signalId, accountId = momoId, type = DetectedType.EXPENSE, fee = null),
            )
            val recorded = repository.recordEntry(entry(accountId = momoId))

            // when
            repository.undoMerge(recorded.mergeId.shouldNotBeNull())

            // then
            recorded().map { Triple(it.status, it.origin, it.note) } shouldContainExactlyInAnyOrder listOf(
                Triple(TransactionStatus.DETECTED, TransactionOrigin.SIGNAL, null),
                Triple(TransactionStatus.CONFIRMED, TransactionOrigin.MANUAL, "Pain"),
            )
            database.transactionMergeDao().getMerge(recorded.mergeId.shouldNotBeNull())?.undoneAt shouldBe
                CLOCK.instant()
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
                    foreignAmount = null,
                    foreignCurrency = null,
                ),
            )
        }
    }

    @Nested
    inner class RecordReconciliation {
        @Test
        fun `should record confirmed cash expenses at the time of the count, which the balance leaves out`() = runTest {
            // given
            val cashCounts = CashCountRepositoryImpl(
                cashCountDao = database.cashCountDao(),
                transactionDao = database.transactionDao(),
                cashBalanceService = CashBalanceServiceImpl(),
            )
            cashCounts.addCount(amount = 3_000, countedAt = Instant.ofEpochSecond(2_000), expectedAmount = 7_500)

            // when
            repository.recordReconciliation(
                expenses = listOf(
                    ReconciledExpense(amount = 2_000, categoryId = null, note = "Moto"),
                    ReconciledExpense(amount = 2_500, categoryId = null, note = null),
                ),
                countedAt = Instant.ofEpochSecond(2_000),
            )

            // then
            recorded().map { Triple(it.origin, it.amount, it.note) } shouldContainExactlyInAnyOrder listOf(
                Triple(TransactionOrigin.RECONCILIATION, 2_000L, "Moto"),
                Triple(TransactionOrigin.RECONCILIATION, 2_500L, null),
            )
            cashCounts.balance.first().shouldNotBeNull().amount shouldBe 3_000
        }
    }

    @Nested
    inner class HasCashWithdrawalSince {
        @Test
        fun `should find a withdrawal after the time`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = null,
                    occurredAt = Instant.ofEpochSecond(2_000),
                    rawSignalId = null,
                ),
            )

            // when
            val withdrawn = repository.hasCashWithdrawalSince(Instant.ofEpochSecond(1_000))

            // then
            withdrawn.shouldBeTrue()
        }

        @Test
        fun `should not count a withdrawal from before the time`() = runTest {
            // given
            val momoId = database.accountDao().insert(momo())
            repository.recordWithdrawal(
                Withdrawal(
                    accountId = momoId,
                    amount = 20_000,
                    fee = null,
                    occurredAt = Instant.ofEpochSecond(500),
                    rawSignalId = null,
                ),
            )

            // when
            val withdrawn = repository.hasCashWithdrawalSince(Instant.ofEpochSecond(1_000))

            // then
            withdrawn.shouldBeFalse()
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

    private fun unparsedSignal() = RawSignalEntity(
        sender = "com.samsung.android.messaging",
        title = "OrangeMoney",
        text = "Paiement de 2500 FCFA",
        receivedAt = Instant.EPOCH,
        templateVersion = null,
    )

    // An operation of 2 500 with Boulangerie, read at second 1 000.
    private fun operation(rawSignalId: Long, accountId: Long, type: DetectedType, fee: Long?) = DetectedOperation(
        rawSignalId = rawSignalId,
        templateVersion = "orange-money.payment@1",
        accountId = accountId,
        type = type,
        amount = 2_500,
        fee = fee,
        foreignAmount = null,
        foreignCurrency = null,
        balance = null,
        counterparty = "Boulangerie",
        occurredAt = Instant.ofEpochSecond(1_000),
    )

    // The payment of 2 500 with Boulangerie, typed by the user ten minutes after it was made.
    private fun entry(accountId: Long) = ManualEntry(
        accountId = accountId,
        type = EntryType.EXPENSE,
        amount = 2_500,
        categoryId = null,
        note = "Pain",
        occurredAt = Instant.ofEpochSecond(1_600),
    )

    private fun withdrawalSignal() = RawSignalEntity(
        sender = "com.mtn.momo",
        title = null,
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
        foreignAmount = null,
        foreignCurrency = null,
    )

    private companion object {
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
