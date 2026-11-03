package com.cauri.core.data.service

import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.model.ReminderAction
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [OutcomeService]. */
@Singleton
internal class OutcomeServiceImpl @Inject constructor() : OutcomeService {
    override fun outcomeOf(decision: DecisionLogEntity, paidByHand: Boolean, now: Instant): DecisionOutcome? {
        val cardGone = decision.transactionId == null && decision.visitId != null
        return when {
            decision.outcome != null -> null
            cardGone && !paidByHand -> DecisionOutcome.NOTHING_SPENT
            cardGone && decision.action == ReminderAction.SKIP -> DecisionOutcome.MISSED
            cardGone -> DecisionOutcome.CONFIRMED
            !decision.decidedAt.plus(OutcomeService.TIMEOUT).isAfter(now) -> DecisionOutcome.TIMEOUT
            else -> null
        }
    }

    override fun accidental(decision: DecisionLogEntity): Boolean {
        val answeredAt = decision.outcomeAt ?: return false
        return decision.excluded == null &&
            decision.action == ReminderAction.NOTIFY_NOW &&
            decision.outcome in SET_ASIDE &&
            Duration.between(decision.decidedAt, answeredAt) < OutcomeService.ACCIDENTAL
    }

    private companion object {
        val SET_ASIDE = setOf(DecisionOutcome.DISMISSED, DecisionOutcome.NOTHING_SPENT)
    }
}
