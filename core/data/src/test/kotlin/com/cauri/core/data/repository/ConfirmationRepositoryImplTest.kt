package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.entity.CategoryEntity
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.DetectedType
import com.cauri.core.data.model.SplitPart
import com.cauri.core.data.model.TransactionEdit
import com.cauri.core.data.service.DuplicateServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

internal class ConfirmationRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var transactions: TransactionRepositoryImpl
    private lateinit var repository: ConfirmationRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        transactions = TransactionRepositoryImpl(
            database.transactionDao(),
            database.transactionMergeDao(),
            database.transferLinkDao(),
            database.accountDao(),
            DuplicateServiceImpl(),
            CLOCK,
        )
        repository = ConfirmationRepositoryImpl(database.confirmationDao(), CLOCK)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Confirm {
        @Test
        fun `should confirm the operation with the category and amount chosen, next to the proposal`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            detect(accountId = omId, type = DetectedType.EXPENSE)
            val pending = transactions.pending.first().single()

            // when
            repository.confirm(operation = pending, categoryId = null, proposedCategoryId = null, amount = 3_000)

            // then
            recorded().map { listOf(it.status, it.amount, it.proposedAmount) } shouldContainExactly
                listOf(listOf(TransactionStatus.CONFIRMED, 3_000L, 2_500L))
            transactions.pending.first().shouldBeEmpty()
        }
    }

    @Nested
    inner class ConfirmEdited {
        @Test
        fun `should split the operation into linked parts, the rest in its category`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            val (food, family) = database.categoryDao().insertAll(listOf(category("Alimentation"), category("Famille")))
            detect(accountId = omId, type = DetectedType.EXPENSE)
            val pending = transactions.pending.first().single()

            // when
            repository.confirm(
                edit(transactionId = pending.transactionId, accountId = omId, categoryId = food)
                    .copy(parts = listOf(SplitPart(categoryId = family, amount = 1_000))),
            )

            // then
            val (kept, part) = recorded().sortedBy { it.id }
            listOf(kept.status, kept.amount, kept.categoryId, kept.proposedAmount) shouldContainExactly
                listOf(TransactionStatus.CONFIRMED, 1_500L, food, 2_500L)
            listOf(
                part.status,
                part.amount,
                part.categoryId,
                part.rawSignalId,
                part.proposedAmount,
            ) shouldContainExactly
                listOf(TransactionStatus.CONFIRMED, 1_000L, family, null, null)
            kept.splitGroup.shouldNotBeNull() shouldBe part.splitGroup
        }

        @Test
        fun `should replace the parts of an operation split again`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            val (food, family) = database.categoryDao().insertAll(listOf(category("Alimentation"), category("Famille")))
            detect(accountId = omId, type = DetectedType.EXPENSE)
            val pending = transactions.pending.first().single()
            val edit = edit(transactionId = pending.transactionId, accountId = omId, categoryId = food)
            repository.confirm(edit.copy(parts = listOf(SplitPart(categoryId = family, amount = 1_000))))

            // when
            repository.confirm(edit.copy(parts = listOf(SplitPart(categoryId = family, amount = 500))))

            // then
            recorded().sortedBy { it.id }.map { it.categoryId to it.amount } shouldContainExactly
                listOf(food to 2_000L, family to 500L)
        }

        @Test
        fun `should keep the account, the time and the note typed`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            detect(accountId = omId, type = DetectedType.EXPENSE)
            val pending = transactions.pending.first().single()

            // when
            repository.confirm(
                edit(transactionId = pending.transactionId, accountId = CASH_ACCOUNT_ID, categoryId = null)
                    .copy(occurredAt = Instant.ofEpochSecond(500), note = " Pain "),
            )

            // then
            recorded().map { listOf(it.accountId, it.occurredAt, it.note) } shouldContainExactly
                listOf(listOf(omId, Instant.ofEpochSecond(500), "Pain"))
        }

        @Test
        fun `should confirm both sides of a withdrawal with the amount typed`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            detect(accountId = omId, type = DetectedType.WITHDRAWAL)
            val pending = transactions.pending.first().single()

            // when
            repository.confirm(edit(transactionId = pending.transactionId, accountId = omId, categoryId = null))

            // then
            recorded().map { listOf(it.type, it.status, it.amount) } shouldContainExactlyInAnyOrder listOf(
                listOf(TransactionType.TRANSFER_OUT, TransactionStatus.CONFIRMED, 2_500L),
                listOf(TransactionType.TRANSFER_IN, TransactionStatus.CONFIRMED, 2_500L),
            )
        }
    }

    @Nested
    inner class Dismiss {
        @Test
        fun `should set both sides of a withdrawal aside`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            detect(accountId = omId, type = DetectedType.WITHDRAWAL)
            val pending = transactions.pending.first().single()

            // when
            repository.dismiss(transactionId = pending.transactionId, type = pending.type)

            // then
            recorded().map { it.status } shouldContainExactlyInAnyOrder
                listOf(TransactionStatus.DISMISSED, TransactionStatus.DISMISSED)
        }
    }

    @Nested
    inner class Reopen {
        @Test
        fun `should put the operation back to confirm with the amount read`() = runTest {
            // given
            val omId = database.accountDao().insert(orangeMoney())
            detect(accountId = omId, type = DetectedType.EXPENSE)
            val pending = transactions.pending.first().single()
            repository.confirm(operation = pending, categoryId = null, proposedCategoryId = null, amount = 3_000)

            // when
            repository.reopen(pending.transactionId)

            // then
            recorded().map { it.status to it.amount } shouldContainExactly listOf(TransactionStatus.DETECTED to 2_500L)
        }
    }

    private suspend fun detect(accountId: Long, type: DetectedType) {
        val signalId = database.rawSignalDao().insert(
            RawSignalEntity(
                sender = "com.samsung.android.messaging",
                title = "OrangeMoney",
                text = "Paiement",
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
                amount = 2_500,
                fee = null,
                foreignAmount = null,
                foreignCurrency = null,
                balance = null,
                counterparty = "BOULANGERIE",
                occurredAt = Instant.ofEpochSecond(1_000),
            ),
        )
    }

    private fun edit(transactionId: Long, accountId: Long, categoryId: Long?) = TransactionEdit(
        transactionId = transactionId,
        amount = 2_500,
        categoryId = categoryId,
        proposedCategoryId = null,
        accountId = accountId,
        occurredAt = Instant.ofEpochSecond(1_000),
        note = null,
        parts = emptyList(),
    )

    private fun category(name: String) = CategoryEntity(
        name = name,
        parentId = null,
        nature = CategoryNature.EXPENSE,
        status = CategoryStatus.ACTIVE,
    )

    private suspend fun recorded(): List<TransactionEntity> =
        TransactionStatus.entries.flatMap { database.transactionDao().observeByStatus(it).first() }

    private fun orangeMoney() = AccountEntity(
        name = "Orange Money",
        kind = AccountKind.MOBILE_MONEY,
        provider = "ORANGE_MONEY",
        currency = "XAF",
        status = AccountStatus.ACTIVE,
    )

    private companion object {
        val CLOCK: Clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
