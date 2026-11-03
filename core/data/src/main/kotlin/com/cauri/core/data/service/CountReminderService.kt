package com.cauri.core.data.service

import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.CountReminderReason
import com.cauri.core.data.model.CountReminderSettings
import java.time.LocalDate
import java.time.ZonedDateTime

/** Rules of the cash count reminders: at most one a day, in the evening, only when the user wants it. */
interface CountReminderService {
    /**
     * Why to remind the user to count the cash at [now], or `null` not to: [settings] chosen by the user, [balance]
     * since the last count (`null` before the first one), [withdrawalToday] whether cash was withdrawn today, and
     * [lastReminder] the day of the last reminder (`null` when none was sent).
     */
    fun reason(
        settings: CountReminderSettings,
        now: ZonedDateTime,
        balance: CashBalance?,
        withdrawalToday: Boolean,
        lastReminder: LocalDate?,
    ): CountReminderReason?
}
