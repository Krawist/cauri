package com.cauri.feature.cash.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cauri.core.data.preferences.UserPreferencesRepository
import com.cauri.core.data.repository.CashCountRepository
import com.cauri.core.data.repository.TransactionRepository
import com.cauri.core.data.service.CountReminderService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first

/**
 * Checks every hour whether to suggest counting the cash ([CountReminderService]) and shows the reminder, at most
 * once a day.
 */
// Injected: the data the rules read, the rules, the notification and the clock.
@Suppress("LongParameterList")
@HiltWorker
internal class CountReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferences: UserPreferencesRepository,
    private val cashCounts: CashCountRepository,
    private val transactions: TransactionRepository,
    private val reminders: CountReminderService,
    private val notifier: CountReminderNotifier,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val now = ZonedDateTime.now(clock)
        val reason = reminders.reason(
            settings = preferences.countReminder.first(),
            now = now,
            balance = cashCounts.balance.first(),
            withdrawalToday = transactions.hasCashWithdrawalSince(now.toLocalDate().atStartOfDay(now.zone).toInstant()),
            lastReminder = preferences.lastCountReminder.first(),
        )
        if (reason != null && notifier.show(reason)) preferences.setLastCountReminder(now.toLocalDate())
        return Result.success()
    }
}
