package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.ReminderAction
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.VisitHistory
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DecisionLogRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: DecisionLogRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = DecisionLogRepositoryImpl(database.decisionLogDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Log {
        @Test
        fun `should keep the decision about the card, its outcome open`() = runTest {
            // given
            val card = database.transactionDao().insert(expense(amount = 0, at = NOON, placeId = null))

            // when
            repository.log(decision(transactionId = card, at = NOON, action = ReminderAction.NOTIFY_NOW))

            // then
            repository.hasDecision(card).shouldBeTrue()
            database.decisionLogDao().getOpen().single().features shouldBe listOf(1.0, 0.5)
        }
    }

    @Nested
    inner class Stats {
        @Test
        fun `should count the notifications of the day and give the last one and the last transaction`() = runTest {
            // given
            val card = database.transactionDao().insert(expense(amount = 2_000, at = NOON, placeId = null))
            repository.log(
                decision(transactionId = card, at = NOON - Duration.ofDays(1), action = ReminderAction.NOTIFY_NOW),
            )
            repository.log(decision(transactionId = card, at = NOON, action = ReminderAction.NOTIFY_NOW))
            repository.log(decision(transactionId = card, at = NOON, action = ReminderAction.SKIP))

            // when
            val stats = repository.stats(dayStart = NOON - Duration.ofHours(11))

            // then
            stats shouldBe ReminderStats(notificationsToday = 1, lastNotifiedAt = NOON, lastTransactionAt = NOON)
        }
    }

    @Nested
    inner class VisitHistoryOf {
        @Test
        fun `should give the visits before to the place and those followed by an expense`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            listOf(1L, 2L, 3L).forEach { day ->
                val entered = NOON - Duration.ofDays(day)
                database.visitDao().insert(
                    VisitEntity(
                        placeId = placeId,
                        enteredAt = entered,
                        leftAt =
                        entered + HALF_HOUR,
                    ),
                )
            }
            database.transactionDao().insert(
                expense(amount = 2_000, at = NOON - Duration.ofDays(2) + HALF_HOUR, placeId = placeId),
            )
            val card = database.transactionDao().insert(expense(amount = 0, at = NOON, placeId = placeId))
            val visitId = database.visitDao().insert(
                VisitEntity(placeId = placeId, enteredAt = NOON - HALF_HOUR, leftAt = NOON, transactionId = card),
            )

            // when
            val history = repository.visitHistory(card)

            // then
            history shouldBe VisitHistory(visitId = visitId, placeVisits = 3, visitsWithExpense = 1)
        }
    }

    private fun decision(transactionId: Long, at: Instant, action: ReminderAction) = NewDecision(
        decidedAt = at,
        transactionId = transactionId,
        visitId = null,
        features = listOf(1.0, 0.5),
        featureVersion = 1,
        action = action,
        probability = 1.0,
        modelVersion = "rules-1",
    )

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
        val NOON: Instant = Instant.parse("2026-11-03T11:00:00Z")
        val HALF_HOUR: Duration = Duration.ofMinutes(30)
    }
}
