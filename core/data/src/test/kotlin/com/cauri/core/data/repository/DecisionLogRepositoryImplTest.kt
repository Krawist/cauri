package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.database.entity.PlaceEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.DecisionExclusion
import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.model.PlaceSpendRate
import com.cauri.core.data.model.ReminderAction
import com.cauri.core.data.model.ReminderOutcome
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.VisitHistory
import com.cauri.core.data.service.OutcomeServiceImpl
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
        repository = DecisionLogRepositoryImpl(database.decisionLogDao(), OutcomeServiceImpl())
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
    inner class AnswerTimes {
        @Test
        fun `should give when the cards were confirmed or found with nothing spent, since the start`() = runTest {
            // given
            val confirmed = database.transactionDao().insert(expense(amount = 2_000, at = NOON, placeId = null))
            val nothing = database.transactionDao().insert(expense(amount = 0, at = NOON, placeId = null))
            val dismissed = database.transactionDao().insert(expense(amount = 500, at = NOON, placeId = null))
            val old = database.transactionDao().insert(expense(amount = 700, at = NOON, placeId = null))
            listOf(confirmed, nothing, dismissed, old).forEach {
                repository.log(decision(transactionId = it, at = NOON, action = ReminderAction.EVENING_SUMMARY))
            }
            val confirmations = database.confirmationDao()
            confirmations.closeDecisions(confirmed, DecisionOutcome.CONFIRMED, NOON + Duration.ofHours(8))
            confirmations.closeDecisions(nothing, DecisionOutcome.NOTHING_SPENT, NOON + Duration.ofHours(7))
            confirmations.closeDecisions(dismissed, DecisionOutcome.DISMISSED, NOON + Duration.ofHours(6))
            confirmations.closeDecisions(old, DecisionOutcome.CONFIRMED, NOON - Duration.ofDays(40))

            // when
            val times = repository.answerTimes(since = NOON - Duration.ofDays(30))

            // then
            times shouldBe listOf(NOON + Duration.ofHours(7), NOON + Duration.ofHours(8))
        }
    }

    @Nested
    inner class AttributeOutcomes {
        @Test
        fun `should time out a card left waiting three days`() = runTest {
            // given
            val card = database.transactionDao().insert(expense(amount = 0, at = NOON, placeId = null))
            repository.log(decision(transactionId = card, at = NOON, action = ReminderAction.NOTIFY_NOW))

            // when
            repository.attributeOutcomes(now = NOON + Duration.ofDays(3))

            // then
            database.decisionLogDao().getSince(Instant.EPOCH).single().outcome shouldBe DecisionOutcome.TIMEOUT
        }

        @Test
        fun `should miss a visit skipped whose expense the user typed by hand`() = runTest {
            // given
            val placeId = database.placeDao().insert(market())
            val card = database.transactionDao().insert(
                expense(amount = 0, at = NOON, placeId = placeId).copy(status = TransactionStatus.DETECTED),
            )
            val visitId = database.visitDao().insert(
                VisitEntity(placeId = placeId, enteredAt = NOON - HALF_HOUR, leftAt = NOON, transactionId = card),
            )
            repository.log(
                decision(transactionId = card, at = NOON, action = ReminderAction.SKIP).copy(visitId = visitId),
            )
            database.transactionDao().insert(
                expense(amount = 1_500, at = NOON - Duration.ofMinutes(15), placeId = placeId),
            )
            database.visitDao().removeWaitingCard(
                VisitEntity(
                    id = visitId,
                    placeId = placeId,
                    enteredAt = NOON - HALF_HOUR,
                    leftAt = NOON,
                    transactionId = card,
                ),
            )

            // when
            repository.attributeOutcomes(now = NOON + HALF_HOUR)

            // then
            database.decisionLogDao().getSince(Instant.EPOCH).single().outcome shouldBe DecisionOutcome.MISSED
        }

        @Test
        fun `should leave out a notification set aside within two seconds`() = runTest {
            // given
            val card = database.transactionDao().insert(expense(amount = 0, at = NOON, placeId = null))
            repository.log(decision(transactionId = card, at = NOON, action = ReminderAction.NOTIFY_NOW))
            database.confirmationDao().closeDecisions(card, DecisionOutcome.DISMISSED, NOON + Duration.ofSeconds(1))

            // when
            repository.attributeOutcomes(now = NOON + HALF_HOUR)

            // then
            database.decisionLogDao().getSince(Instant.EPOCH).single().excluded shouldBe DecisionExclusion.ACCIDENTAL
        }

        @Test
        fun `should let a late answer correct a time out`() = runTest {
            // given
            val card = database.transactionDao().insert(expense(amount = 0, at = NOON, placeId = null))
            repository.log(decision(transactionId = card, at = NOON, action = ReminderAction.EVENING_SUMMARY))
            repository.attributeOutcomes(now = NOON + Duration.ofDays(3))

            // when
            database.confirmationDao().closeDecisions(card, DecisionOutcome.CONFIRMED, NOON + Duration.ofDays(4))

            // then
            database.decisionLogDao().getSince(Instant.EPOCH).single().outcome shouldBe DecisionOutcome.CONFIRMED
        }
    }

    @Nested
    inner class TrainingSet {
        @Test
        fun `should give the decisions closed and counted, since the start`() = runTest {
            // given
            val cards = (1..4).map {
                database.transactionDao().insert(expense(amount = 1_000, at = NOON, placeId = null))
            }
            cards.forEach { repository.log(decision(transactionId = it, at = NOON, action = ReminderAction.SKIP)) }
            repository.log(
                decision(transactionId = cards[3], at = NOON - Duration.ofDays(100), action = ReminderAction.SKIP),
            )
            val confirmations = database.confirmationDao()
            confirmations.closeDecisions(cards[0], DecisionOutcome.CONFIRMED, NOON + HALF_HOUR)
            confirmations.closeDecisions(cards[1], DecisionOutcome.DISMISSED, NOON + Duration.ofSeconds(1))
            confirmations.closeDecisions(cards[3], DecisionOutcome.NOTHING_SPENT, NOON + HALF_HOUR)
            database.decisionLogDao().getSince(Instant.EPOCH).filter { it.transactionId == cards[1] }.forEach {
                database.decisionLogDao().update(it.copy(excluded = DecisionExclusion.ACCIDENTAL))
            }

            // when
            val training = repository.trainingSet(since = NOON - Duration.ofDays(90))

            // then
            training.map { it.outcome } shouldBe listOf(ReminderOutcome.CONFIRMED, ReminderOutcome.NOTHING_SPENT)
        }
    }

    @Nested
    inner class PlaceSpendRates {
        @Test
        fun `should tell how often the visits to each shop led to an expense there`() = runTest {
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

            // when
            val rates = repository.placeSpendRates(since = NOON - Duration.ofDays(30))

            // then
            rates shouldBe listOf(PlaceSpendRate(name = "Marché Mokolo", visits = 3, withExpense = 1))
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
