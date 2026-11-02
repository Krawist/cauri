package com.cauri.core.data.database.dao

import android.database.SQLException
import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.AccountKind
import com.cauri.core.data.database.entity.AccountStatus
import com.cauri.core.data.database.entity.CategoryStatus
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TransactionDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var transactionDao: TransactionDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        transactionDao = database.transactionDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Insert {
        @Test
        fun `should refuse a second transaction from the same raw signal`() = runTest {
            // given
            val signalId = database.rawSignalDao().insert(
                RawSignalEntity(
                    sender = "com.mtn.momo",
                    text = "Paiement de 5000 FCFA",
                    receivedAt = Instant.EPOCH,
                    templateVersion = "momo-1",
                ),
            )
            val detected = expense(amount = 5_000, rawSignalId = signalId)
            transactionDao.insert(detected)

            // when & then
            shouldThrow<SQLException> { transactionDao.insert(detected) }
        }

        @Test
        fun `should accept several transactions without a raw signal`() = runTest {
            // given
            transactionDao.insert(expense(amount = 500, rawSignalId = null))

            // when
            transactionDao.insert(expense(amount = 500, rawSignalId = null))

            // then
            transactionDao.observeByStatus(TransactionStatus.CONFIRMED).first() shouldHaveSize 2
        }
    }

    @Nested
    inner class GetByTransferGroup {
        @Test
        fun `should return both sides of a transfer`() = runTest {
            // given
            val momoId = database.accountDao().insert(
                AccountEntity(
                    name = "MTN MoMo",
                    kind = AccountKind.MOBILE_MONEY,
                    provider = "MTN",
                    currency = "XAF",
                    status = AccountStatus.ACTIVE
                ),
            )
            val out = transfer(accountId = momoId, type = TransactionType.TRANSFER_OUT, group = "withdrawal-1")
            val into = transfer(accountId = CASH_ACCOUNT_ID, type = TransactionType.TRANSFER_IN, group = "withdrawal-1")
            val outId = transactionDao.insert(out)
            val intoId = transactionDao.insert(into)
            transactionDao.insert(
                transfer(accountId = CASH_ACCOUNT_ID, type = TransactionType.TRANSFER_IN, group = "other"),
            )

            // when
            val sides = transactionDao.getByTransferGroup("withdrawal-1")

            // then
            sides shouldContainExactlyInAnyOrder listOf(out.copy(id = outId), into.copy(id = intoId))
        }
    }

    @Nested
    inner class ObserveByStatus {
        @Test
        fun `should list the transactions with the status, newest first`() = runTest {
            // given
            val older = expense(amount = 300, rawSignalId = null, status = TransactionStatus.DETECTED, at = 1_000)
            val newer = expense(amount = 700, rawSignalId = null, status = TransactionStatus.DETECTED, at = 2_000)
            val olderId = transactionDao.insert(older)
            val newerId = transactionDao.insert(newer)
            transactionDao.insert(
                expense(amount = 900, rawSignalId = null, status = TransactionStatus.CONFIRMED, at = 3_000),
            )

            // when
            val detected = transactionDao.observeByStatus(TransactionStatus.DETECTED).first()

            // then
            detected shouldContainExactly listOf(newer.copy(id = newerId), older.copy(id = olderId))
        }
    }

    // A cash expense: only the fields the tests vary are parameters.
    private fun expense(
        amount: Long,
        rawSignalId: Long?,
        status: TransactionStatus = TransactionStatus.CONFIRMED,
        at: Long = 0,
    ) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = status,
        origin = if (rawSignalId == null) TransactionOrigin.MANUAL else TransactionOrigin.SIGNAL,
        amount = amount,
        occurredAt = Instant.ofEpochMilli(at),
        categoryId = null,
        placeId = null,
        rawSignalId = rawSignalId,
        transferGroup = null,
        counterparty = null,
        note = null,
    )

    private fun transfer(accountId: Long, type: TransactionType, group: String) = TransactionEntity(
        accountId = accountId,
        type = type,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.SIGNAL,
        amount = 20_000,
        occurredAt = Instant.EPOCH,
        categoryId = null,
        placeId = null,
        rawSignalId = null,
        transferGroup = group,
        counterparty = null,
        note = null,
    )
}
