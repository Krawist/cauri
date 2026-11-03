package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.DecisionLogDao
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.model.DecisionExclusion
import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.VisitHistory
import com.cauri.core.data.service.OutcomeService
import com.cauri.core.data.service.VisitService
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [DecisionLogRepository]. */
@Singleton
internal class DecisionLogRepositoryImpl @Inject constructor(
    private val decisionLogDao: DecisionLogDao,
    private val outcomes: OutcomeService,
) : DecisionLogRepository {
    override suspend fun hasDecision(transactionId: Long) = decisionLogDao.hasDecision(transactionId)

    override suspend fun stats(dayStart: Instant) = ReminderStats(
        notificationsToday = decisionLogDao.countNotifiedSince(dayStart),
        lastNotifiedAt = decisionLogDao.getLastNotifiedAt(),
        lastTransactionAt = decisionLogDao.getLastTransactionAt(),
    )

    override suspend fun visitHistory(transactionId: Long): VisitHistory? {
        val visit = decisionLogDao.getVisitOfCard(transactionId) ?: return null
        return VisitHistory(
            visitId = visit.id,
            placeVisits = decisionLogDao.countVisits(placeId = visit.placeId, exceptVisit = visit.id),
            visitsWithExpense = decisionLogDao.countVisitsWithExpense(placeId = visit.placeId, exceptVisit = visit.id),
        )
    }

    override suspend fun answerTimes(since: Instant): List<Instant> = decisionLogDao.getAnswerTimes(since)

    override suspend fun attributeOutcomes(now: Instant) {
        decisionLogDao.getOpen().forEach { decision ->
            val outcome = outcomes.outcomeOf(decision = decision, paidByHand = paidByHand(decision), now = now)
            if (outcome != null) decisionLogDao.update(decision.copy(outcome = outcome, outcomeAt = now))
        }
        decisionLogDao.getSince(now - OutcomeService.TIMEOUT).filter(outcomes::accidental).forEach {
            decisionLogDao.update(it.copy(excluded = DecisionExclusion.ACCIDENTAL))
        }
    }

    // Whether the visit of [decision] was paid by an expense typed there, from its entry to just after its exit.
    private suspend fun paidByHand(decision: DecisionLogEntity): Boolean {
        val visit = decision.visitId?.let { decisionLogDao.getVisit(it) }
        val left = visit?.leftAt ?: return false
        return decisionLogDao.hasExpenseTypedAt(
            placeId = visit.placeId,
            from = visit.enteredAt,
            to = left + VisitService.AFTER_EXIT,
        )
    }

    override suspend fun log(decision: NewDecision) {
        decisionLogDao.insert(
            DecisionLogEntity(
                decidedAt = decision.decidedAt,
                visitId = decision.visitId,
                transactionId = decision.transactionId,
                features = decision.features,
                featureVersion = decision.featureVersion,
                action = decision.action,
                probability = decision.probability,
                modelVersion = decision.modelVersion,
                outcome = null,
                outcomeAt = null,
                excluded = decision.excluded,
            ),
        )
    }
}
