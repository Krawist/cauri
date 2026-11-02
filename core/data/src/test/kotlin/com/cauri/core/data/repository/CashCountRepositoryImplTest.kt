package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.service.CashBalanceServiceImpl
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CashCountRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: CashCountRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = CashCountRepositoryImpl(
            cashCountDao = database.cashCountDao(),
            transactionDao = database.transactionDao(),
            cashBalanceService = CashBalanceServiceImpl(),
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Balance {
        @Test
        fun `should be null before the first count`() = runTest {
            // when
            val balance = repository.balance.first()

            // then
            balance.shouldBeNull()
        }

        @Test
        fun `should start from the latest count and take away the cash expenses since`() = runTest {
            // given
            database.cashCountDao().insert(
                CashCountEntity(countedAt = Instant.ofEpochSecond(1_000), amount = 5_000, expectedAmount = null),
            )
            database.cashCountDao().insert(
                CashCountEntity(countedAt = Instant.ofEpochSecond(2_000), amount = 7_500, expectedAmount = 6_000),
            )
            database.transactionDao().insert(
                TransactionEntity(
                    accountId = CASH_ACCOUNT_ID,
                    type = TransactionType.EXPENSE,
                    status = TransactionStatus.CONFIRMED,
                    origin = TransactionOrigin.MANUAL,
                    amount = 500,
                    occurredAt = Instant.ofEpochSecond(3_000),
                    categoryId = null,
                    placeId = null,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = null,
                    note = null,
                ),
            )

            // when
            val balance = repository.balance.first()

            // then
            balance shouldBe
                CashBalance(amount = 7_000, countedAt = Instant.ofEpochSecond(2_000), expensesSinceCount = 1)
        }
    }

    @Nested
    inner class AddCount {
        @Test
        fun `should save the count`() = runTest {
            // when
            repository.addCount(amount = 7_500, countedAt = Instant.ofEpochMilli(1_000), expectedAmount = null)

            // then
            database.cashCountDao().getLatest().shouldNotBeNull().shouldBeEqualToIgnoringFields(
                CashCountEntity(countedAt = Instant.ofEpochMilli(1_000), amount = 7_500, expectedAmount = null),
                CashCountEntity::id,
            )
        }
    }
}
