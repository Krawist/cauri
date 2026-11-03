package com.cauri.core.capture.reminder

import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.DecisionExclusion
import com.cauri.core.data.model.NewDecision
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.ReminderAction as LoggedAction
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.DecisionLogRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.repository.VisitRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Default implementation for [ReminderDecider]. */
// Injected: the cards, the past, the cash, where the user is, their limits, the guardrails, the rules, their
// numbers, the notification and the clock.
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
    private val encoder: FeatureEncoder,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) : ReminderDecider {
    // The listener and the geofences may call at the same time: a card is decided once.
    private val mutex = Mutex()

    override suspend fun decidePending() = mutex.withLock {
        val now = clock.instant()
        val today = ZonedDateTime.now(clock)
        val stats = log.stats(dayStart = today.toLocalDate().atStartOfDay(today.zone).toInstant())
        var lastNotifiedAt = stats.lastNotifiedAt
        var notified = stats.notificationsToday
        val cash = cashCounts.balance.first()?.amount
        val atHomeOrWork = visits.atHomeOrWork()
        val limits = preferences.reminders.first().let {
            ReminderLimits(
                quietFrom = it.quietFrom,
                quietUntil = it.quietUntil,
                dailyCap = it.dailyCap,
                visitReminders = it.visitReminders,
            )
        }
        // The oldest first, so that the spacing between notifications holds; an old card is no news to remind.
        transactions.pending.first()
            .filter { !it.occurredAt.isBefore(now - RECENT) && !log.hasDecision(it.transactionId) }
            .sortedBy { it.occurredAt }
            .forEach { operation ->
                val history = operation.stayMinutes?.let { log.visitHistory(operation.transactionId) }
                val context = ReminderContext(
                    trigger = trigger(operation),
                    at = today,
                    expense = operation.type == PendingType.EXPENSE,
                    stayMinutes = operation.stayMinutes,
                    placeVisits = history?.placeVisits ?: 0,
                    placeSpendRate = history?.takeIf { it.placeVisits > 0 }
                        ?.let { it.visitsWithExpense.toDouble() / it.placeVisits },
                    minutesSinceTransaction = stats.lastTransactionAt?.let { minutes(it, now) },
                    minutesSinceNotification = lastNotifiedAt?.let { minutes(it, now) },
                    notificationsToday = notified,
                    cashBalance = cash,
                    atHomeOrWork = atHomeOrWork,
                )
                // The guardrails first: the rules choose among what they allow.
                val action = guardrails.restrict(rules.decide(context), guardrails.allowed(context, limits))
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
                        features = encoder.encode(context, rulesScore = rules.score(action)),
                        featureVersion = FeatureEncoder.VERSION,
                        action = LoggedAction.valueOf(action.name),
                        // The rules decide for sure.
                        probability = 1.0,
                        modelVersion = ReminderRules.VERSION,
                        excluded = DecisionExclusion.UNSEEN.takeIf { action == ReminderAction.NOTIFY_NOW && !shown },
                    ),
                )
            }
    }

    private fun trigger(operation: PendingOperation) = when {
        operation.stayMinutes != null -> ReminderTrigger.VISIT_END
        operation.type == PendingType.TRANSFER && operation.toAccountKind == AccountKind.CASH ->
            ReminderTrigger.WITHDRAWAL
        else -> ReminderTrigger.DETECTED_OPERATION
    }

    private fun minutes(from: Instant, to: Instant) = Duration.between(from, to).toMinutes()

    private companion object {
        // A card older than two hours, such as those waiting when Cauri is installed, is no news to remind.
        val RECENT: Duration = Duration.ofHours(2)
    }
}
