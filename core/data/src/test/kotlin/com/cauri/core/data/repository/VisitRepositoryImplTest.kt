package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.ReminderAction
import com.cauri.core.data.service.VisitServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VisitRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: VisitRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = VisitRepositoryImpl(database.visitDao(), database.cashCountDao(), VisitServiceImpl())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Leave {
        @Test
        fun `should ask what was spent at a shop left with nothing paid, its usual amount proposed`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            database.transactionDao().insert(
                expense(amount = 2_000, at = ENTERED - Duration.ofDays(3), placeId = placeId),
            )
            database.visitDao().enter(placeId = placeId, enteredAt = ENTERED)

            // when
            repository.leave(placeId = placeId, at = LEFT)

            // then
            val card = database.transactionDao().getById(card(placeId)).shouldNotBeNull()
            listOf(card.status, card.origin, card.amount, card.proposedAmount, card.counterparty) shouldBe
                listOf(TransactionStatus.DETECTED, TransactionOrigin.VISIT, 0L, 2_000L, "Marché Mokolo")
        }

        @Test
        fun `should ask nothing about a shop where a payment was made`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            database.visitDao().enter(placeId = placeId, enteredAt = ENTERED)
            val paid = database.transactionDao().insert(expense(amount = 2_500, at = LEFT, placeId = null))

            // when
            repository.leave(placeId = placeId, at = LEFT)

            // then
            database.visitDao().getRecent(Instant.EPOCH).single().transactionId.shouldBeNull()
            database.transactionDao().getById(paid).shouldNotBeNull().placeId shouldBe placeId
        }

        @Test
        fun `should ask nothing about home`() = runTest {
            // given
            val placeId = database.placeDao().insert(market().copy(kind = PlaceKind.HOME, remindAfterVisit = false))
            database.visitDao().enter(placeId = placeId, enteredAt = ENTERED)

            // when
            repository.leave(placeId = placeId, at = LEFT)

            // then
            database.visitDao().getRecent(Instant.EPOCH).single().transactionId.shouldBeNull()
        }
    }

    @Nested
    inner class AttachTransactions {
        @Test
        fun `should take back the card of a visit once a message tells the payment, come late`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            database.visitDao().enter(placeId = placeId, enteredAt = ENTERED)
            repository.leave(placeId = placeId, at = LEFT)
            val card = card(placeId)
            database.transactionDao().insert(
                expense(amount = 2_500, at = LEFT + Duration.ofMinutes(10), placeId = null),
            )

            // when
            repository.attachTransactions(LEFT + Duration.ofMinutes(20))

            // then
            database.transactionDao().getById(card).shouldBeNull()
        }
    }

    @Nested
    inner class Settle {
        @Test
        fun `should confirm the cards typed and set aside the others`() = runTest {
            // given
            val marketId = database.placeDao().insert(market())
            val bakeryId = database.placeDao().insert(market().copy(name = "Boulangerie", latitude = 3.9))
            listOf(marketId, bakeryId).forEach {
                database.visitDao().enter(placeId = it, enteredAt = ENTERED)
                repository.leave(placeId = it, at = LEFT)
            }

            // when
            repository.settle(amounts = mapOf(card(marketId) to 3_000L), at = LEFT + Duration.ofHours(2))

            // then
            database.transactionDao().getById(card(marketId)).shouldNotBeNull().let { it.status to it.amount } shouldBe
                (TransactionStatus.CONFIRMED to 3_000L)
            database.transactionDao().getById(card(bakeryId)).shouldNotBeNull().status shouldBe
                TransactionStatus.DISMISSED
            repository.waiting.first().shouldBeEmpty()
        }

        @Test
        fun `should miss a visit skipped whose amount the count found, and time out those set aside`() = runTest {
            // given
            val marketId = database.placeDao().insert(market())
            val bakeryId = database.placeDao().insert(market().copy(name = "Boulangerie", latitude = 3.9))
            listOf(marketId, bakeryId).forEach {
                database.visitDao().enter(placeId = it, enteredAt = ENTERED)
                repository.leave(placeId = it, at = LEFT)
                database.decisionLogDao().insert(
                    DecisionLogEntity(
                        decidedAt = LEFT,
                        visitId = null,
                        transactionId = card(it),
                        features = listOf(1.0),
                        featureVersion = 1,
                        action = ReminderAction.SKIP,
                        probability = 1.0,
                        modelVersion = "rules-2",
                        outcome = null,
                        outcomeAt = null,
                    ),
                )
            }

            // when
            repository.settle(amounts = mapOf(card(marketId) to 3_000L), at = LEFT + Duration.ofHours(2))

            // then
            database.decisionLogDao().getSince(Instant.EPOCH).associate { it.transactionId to it.outcome } shouldBe
                mapOf(card(marketId) to DecisionOutcome.MISSED, card(bakeryId) to DecisionOutcome.TIMEOUT)
        }
    }

    // The card the visit to the place [placeId] created.
    private suspend fun card(placeId: Long): Long =
        database.visitDao().getRecent(Instant.EPOCH).single { it.placeId == placeId }.transactionId.shouldNotBeNull()

    private fun market() = PlaceEntity(
        name = "Marché Mokolo",
        latitude = 3.88,
        longitude = 11.5,
        radiusMeters = 80f,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
    )

    private fun expense(amount: Long, at: Instant, placeId: Long?) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = amount,
        occurredAt = at,
        categoryId = null,
        placeId = placeId,
        rawSignalId = null,
        transferGroup = null,
        counterparty = null,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )

    private companion object {
        val ENTERED: Instant = Instant.parse("2026-11-03T09:00:00Z")
        val LEFT: Instant = Instant.parse("2026-11-03T09:25:00Z")
    }
}
