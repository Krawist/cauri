package com.cauri.core.data.model

/**
 * The user's choice for the cash count reminders.
 *
 * @property frequency How often a reminder may come.
 * @property hour Hour of the evening from which it comes, from 0 to 23.
 */
data class CountReminderSettings(val frequency: CountReminderFrequency, val hour: Int)
