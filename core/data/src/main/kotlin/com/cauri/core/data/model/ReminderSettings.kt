package com.cauri.core.data.model

import java.time.LocalTime

/**
 * The user's limits on the smart reminders.
 *
 * @property quietFrom Hour the quiet hours start, from 0 to 23.
 * @property quietUntil Hour the quiet hours end, from 0 to 23; equal to [quietFrom] for no quiet hours.
 * @property dailyCap Most reminders notified in a day.
 * @property visitReminders Whether a visit to a shop may bring a reminder.
 * @property summaryTime Time of the evening summary the user chose; `null` to let it follow when they answer.
 */
data class ReminderSettings(
    val quietFrom: Int,
    val quietUntil: Int,
    val dailyCap: Int,
    val visitReminders: Boolean,
    val summaryTime: LocalTime? = null,
)
