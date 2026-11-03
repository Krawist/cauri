package com.cauri.core.data.service

import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderReason
import com.cauri.core.data.model.CountReminderSettings
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CountReminderService]. */
@Singleton
internal class CountReminderServiceImpl @Inject constructor() : CountReminderService {
    override fun reason(
        settings: CountReminderSettings,
        now: ZonedDateTime,
        balance: CashBalance?,
        withdrawalToday: Boolean,
        lastReminder: LocalDate?,
    ): CountReminderReason? {
        val today = now.toLocalDate()
        val countedAt = balance?.countedAt?.atZone(now.zone)
        val allowed = settings.frequency != CountReminderFrequency.NEVER &&
            now.hour in settings.hour until QUIET_HOUR &&
            lastReminder != today &&
            countedAt != null &&
            countedAt.toLocalDate() != today
        if (!allowed) return null
        val useful = when {
            withdrawalToday -> CountReminderReason.WITHDRAWAL
            balance.expensesSinceCount >= EXPENSES_BEFORE_REMINDER -> CountReminderReason.EXPENSES
            ChronoUnit.DAYS.between(countedAt.toLocalDate(), today) >= DAYS_BEFORE_REMINDER ->
                CountReminderReason.DAYS_WITHOUT_COUNT
            else -> null
        }
        return useful
            ?: CountReminderReason.EVENING.takeIf { settings.frequency == CountReminderFrequency.EVERY_EVENING }
    }

    private companion object {
        // No reminder from this hour on: too late in the evening.
        const val QUIET_HOUR = 23
        const val EXPENSES_BEFORE_REMINDER = 3
        const val DAYS_BEFORE_REMINDER = 3L
    }
}
