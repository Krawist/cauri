package com.cauri.core.data.model

import java.time.Instant

/**
 * What the reminder engine reads of the past when it decides.
 *
 * @property notificationsToday Reminders notified since the start of the day.
 * @property lastNotifiedAt When the last reminder was notified; `null` before the first.
 * @property lastTransactionAt When the last transaction counted happened; `null` before the first.
 */
data class ReminderStats(val notificationsToday: Int, val lastNotifiedAt: Instant?, val lastTransactionAt: Instant?)
