package com.cauri.core.capture.reminder

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.DecisionExclusion
import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.ReminderAction as LoggedAction
import com.cauri.core.data.model.VisitHistory
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.DecisionLogRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.repository.VisitRepository
import com.cauri.core.engine.Bandit
import com.cauri.core.engine.FeatureEncoder
import com.cauri.core.engine.ReminderAction
import com.cauri.core.engine.ReminderContext
import com.cauri.core.engine.ReminderGuardrails
import com.cauri.core.engine.ReminderLimits
import com.cauri.core.engine.ReminderRules
import com.cauri.core.engine.ReminderTrigger
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Default implementation for [ReminderDecider]. */
// Injected: the cards, the past, the cash, where the user is, their limits, the guardrails, the rules, the bandit and
// what it learnt, their numbers, the notification, the clock and the draws.
@Suppress("LongParameterList")
@Singleton
internal class ReminderDeciderImpl @Inject constructor(
    private val transactions: TransactionRepository,
    private val log: DecisionLogRepository,
    private val cashCounts: CashCountRepository,
    private val visits: VisitRepository,
    private val preferences: UserPreferencesRepository,
    private val guardrails: ReminderGuardrails,
    private val rules: ReminderRules,
    private val bandit: Bandit,
    private val learner: ReminderLearner,
    private val encoder: FeatureEncoder,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
    private val random: Random,
) : ReminderDecider {
    // The listener and the geofences may call at the same time: a card is decided once.
    private val mutex = Mutex()

    override suspend fun decidePending() = mutex.withLock {
        val now = clock.instant()
        val today = ZonedDateTime.now(clock)
        val stats = log.stats(dayStart = today.toLocalDate().atStartOfDay(today.zone).toInstant())
        var lastNotifiedAt = stats.lastNotifiedAt
        var notified = stats.notificationsToday
        val published = learner.current()
        val limits = limits()
        val base = ReminderContext(
            trigger = ReminderTrigger.DETECTED_OPERATION,
            at = today,
            expense = false,
            stayMinutes = null,
            placeVisits = 0,
            placeSpendRate = null,
            minutesSinceTransaction = stats.lastTransactionAt?.let { minutes(it, now) },
            minutesSinceNotification = null,
            notificationsToday = 0,
            cashBalance = cashCounts.balance.first()?.amount,
            atHomeOrWork = visits.atHomeOrWork(),
        )
        // The oldest first, so that the spacing between notifications holds; an old card is no news to remind.
        transactions.pending.first()
            .filter { !it.occurredAt.isBefore(now - RECENT) && !log.hasDecision(it.transactionId) }
            .sortedBy { it.occurredAt }
            .forEach { operation ->
                val history = operation.stayMinutes?.let { log.visitHistory(operation.transactionId) }
                val context = cardContext(base = base, operation = operation, history = history).copy(
                    minutesSinceNotification = lastNotifiedAt?.let { minutes(it, now) },
                    notificationsToday = notified,
                )
                // The guardrails first: the rules, then the bandit once it learnt enough, choose among what they allow.
                val allowed = guardrails.allowed(context, limits)
                val ruled = guardrails.restrict(rules.decide(context), allowed)
                val features = encoder.encode(context, rulesScore = rules.score(ruled))
                val choice = published?.let { bandit.choose(it.model, features, allowed, random) }
                val action = choice?.action ?: ruled
                // A notification Android does not show says nothing of the reminder: it is left out of the training.
                val shown = action == ReminderAction.NOTIFY_NOW && notifier.show(operation)
                if (shown) {
                    lastNotifiedAt = now
                    notified++
                }
                log.log(
                    NewDecision(
                        decidedAt = now,
                        transactionId = operation.transactionId,
                        visitId = history?.visitId,
                        features = features,
                        featureVersion = FeatureEncoder.VERSION,
                        action = LoggedAction.valueOf(action.name),
                        // The rules decide for sure; the bandit tells how often it would.
                        probability = choice?.probability ?: 1.0,
                        modelVersion = published?.let { "$BANDIT_PREFIX${it.version}" } ?: ReminderRules.VERSION,
                        excluded = DecisionExclusion.UNSEEN.takeIf { action == ReminderAction.NOTIFY_NOW && !shown },
                    ),
                )
            }
    }

    // The user's limits, as the guardrails read them.
    private suspend fun limits() = preferences.reminders.first().let {
        ReminderLimits(
            quietFrom = it.quietFrom,
            quietUntil = it.quietUntil,
            dailyCap = it.dailyCap,
            visitReminders = it.visitReminders,
        )
    }

    // What [base] knows of the moment, with what the card of [operation] and its visit's [history] tell.
    private fun cardContext(base: ReminderContext, operation: PendingOperation, history: VisitHistory?) = base.copy(
        trigger = trigger(operation),
        expense = operation.type == PendingType.EXPENSE,
        stayMinutes = operation.stayMinutes,
        placeVisits = history?.placeVisits ?: 0,
        placeSpendRate = history?.takeIf {
            it.placeVisits > 0
        }?.let { it.visitsWithExpense.toDouble() / it.placeVisits },
    )

    private fun trigger(operation: PendingOperation) = when {
        operation.stayMinutes != null -> ReminderTrigger.VISIT_END
        operation.type == PendingType.TRANSFER && operation.toAccountKind == AccountKind.CASH ->
            ReminderTrigger.WITHDRAWAL
        else -> ReminderTrigger.DETECTED_OPERATION
    }

    private fun minutes(from: Instant, to: Instant) = Duration.between(from, to).toMinutes()

    private companion object {
        // « bandit-3 »: the version of the model that decided.
        const val BANDIT_PREFIX = "bandit-"

        // A card older than two hours, such as those waiting when Cauri is installed, is no news to remind.
        val RECENT: Duration = Duration.ofHours(2)
    }
}
