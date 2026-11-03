package com.cauri.core.engine

/**
 * The limits the user sets on the reminders, applied by [ReminderGuardrails] before any decision.
 *
 * @property quietFrom Hour the quiet hours start, from 0 to 23.
 * @property quietUntil Hour the quiet hours end, from 0 to 23; equal to [quietFrom] for no quiet hours.
 * @property dailyCap Most reminders notified in a day.
 * @property visitReminders Whether a visit to a shop may bring a reminder at all.
 */
data class ReminderLimits(val quietFrom: Int, val quietUntil: Int, val dailyCap: Int, val visitReminders: Boolean)
