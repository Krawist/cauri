package com.cauri.core.capture.reminder

import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.DecisionLogRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.engine.SummaryTime
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/** Default implementation for [EveningSummarySender]. */
// Injected: the cards, the answers, the user's choice, the time rule, the notification and the clock.
@Suppress("LongParameterList")
@Singleton
internal class EveningSummarySenderImpl @Inject constructor(
    private val transactions: TransactionRepository,
    private val log: DecisionLogRepository,
    private val preferences: UserPreferencesRepository,
    private val summaryTime: SummaryTime,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) : EveningSummarySender {
    override suspend fun sendIfDue() {
        val now = ZonedDateTime.now(clock)
        val today = now.toLocalDate()
        if (preferences.lastEveningSummary.first() == today || now.toLocalTime() >= LAST) return
        val answers = log.answerTimes(since = now.toInstant() - WINDOW).map { it.atZone(now.zone).toLocalTime() }
        val at = summaryTime.at(chosen = preferences.reminders.first().summaryTime, answers = answers)
        if (now.toLocalTime() < at) return
        val waiting = transactions.pending.first().size
        if (waiting > 0 && notifier.showSummary(waiting)) preferences.setLastEveningSummary(today)
    }

    private companion object {
        // When the user answered over the last month.
        val WINDOW: Duration = Duration.ofDays(30)

        // Too late to ask: the cards wait for tomorrow.
        val LAST: LocalTime = LocalTime.of(23, 0)
    }
}
