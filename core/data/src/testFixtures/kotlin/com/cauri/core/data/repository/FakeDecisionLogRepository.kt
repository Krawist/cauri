package com.cauri.core.data.repository

import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.PlaceSpendRate
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.TrainingDecision
import com.cauri.core.data.model.VisitHistory
import java.time.Instant

/** In-memory [DecisionLogRepository] for tests: it keeps the decisions logged and answers the stats of [stats]. */
class FakeDecisionLogRepository : DecisionLogRepository {
    val decisions = mutableListOf<NewDecision>()

    /** The past the engine reads. */
    var stats = ReminderStats(notificationsToday = 0, lastNotifiedAt = null, lastTransactionAt = null)

    /** The history of each visit card, by card. */
    val histories = mutableMapOf<Long, VisitHistory>()

    override suspend fun hasDecision(transactionId: Long) = decisions.any { it.transactionId == transactionId }

    override suspend fun stats(dayStart: Instant) = stats

    override suspend fun visitHistory(transactionId: Long) = histories[transactionId]

    /** What [answerTimes] answers, whatever the start. */
    var answers = listOf<Instant>()

    override suspend fun answerTimes(since: Instant) = answers

    /** When [attributeOutcomes] ran, in order. */
    val attributions = mutableListOf<Instant>()

    override suspend fun attributeOutcomes(now: Instant) {
        attributions += now
    }

    /** What [trainingSet] answers, whatever the start. */
    var training = listOf<TrainingDecision>()

    /** What [placeSpendRates] answers, whatever the start. */
    var rates = listOf<PlaceSpendRate>()

    override suspend fun trainingSet(since: Instant) = training

    override suspend fun placeSpendRates(since: Instant) = rates

    override suspend fun log(decision: NewDecision) {
        decisions += decision
    }
}
