package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.ProposalSource
import com.cauri.core.data.service.ProposalServiceImpl
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ProposalRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: ProposalRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = ProposalRepositoryImpl(database.transactionDao(), ProposalServiceImpl())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Acceptance {
        @Test
        fun `should measure the confirmed proposals, leaving out those waiting`() = runTest {
            // given
            database.transactionDao().insertAll(
                listOf(
                    expense(status = TransactionStatus.CONFIRMED, amount = 2_500),
                    expense(status = TransactionStatus.CONFIRMED, amount = 3_000),
                    expense(status = TransactionStatus.DETECTED, amount = 2_500),
                ),
            )

            // when
            val acceptance = repository.acceptance(ProposalSource.MERCHANT)

            // then
            acceptance shouldBe mapOf("BOULANGERIE" to 0.5)
        }
    }

    // A cash expense with Boulangerie, for which Cauri proposed 2 500.
    private fun expense(status: TransactionStatus, amount: Long) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = status,
        origin = TransactionOrigin.MANUAL,
        amount = amount,
        occurredAt = Instant.ofEpochSecond(1_000),
        categoryId = null,
        placeId = null,
        rawSignalId = null,
        transferGroup = null,
        counterparty = "BOULANGERIE",
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
        proposedAmount = 2_500,
    )
}
