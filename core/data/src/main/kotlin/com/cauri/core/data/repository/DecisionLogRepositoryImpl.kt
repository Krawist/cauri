package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.DecisionLogDao
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.ReminderStats
import com.cauri.core.data.model.VisitHistory
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [DecisionLogRepository]. */
@Singleton
internal class DecisionLogRepositoryImpl @Inject constructor(private val decisionLogDao: DecisionLogDao) :
    DecisionLogRepository {
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
            ),
        )
    }
}
