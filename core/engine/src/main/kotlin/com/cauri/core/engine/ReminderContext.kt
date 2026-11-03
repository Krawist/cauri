package com.cauri.core.engine

import java.time.ZonedDateTime

/**
 * What the reminder engine knows when it decides, all as numbers: the bandit learns from them.
 *
 * @property trigger What calls the engine.
 * @property at When it decides, in the user's zone.
 * @property expense Whether the card is an expense, rather than an income or a transfer.
 * @property stayMinutes How long the visit lasted, in minutes; `null` when not a visit.
 * @property placeVisits How many visits to the place were left before; 0 when not a visit.
 * @property placeSpendRate Share of those visits followed by an expense there, from 0 to 1; `null` before the first.
 * @property minutesSinceTransaction Minutes since the last transaction counted; `null` before the first.
 * @property minutesSinceNotification Minutes since the last reminder notified; `null` before the first.
 * @property notificationsToday Reminders notified since midnight.
 * @property cashBalance Cash estimated in the pocket, in minor units; `null` before the first count.
 */
data class ReminderContext(
    val trigger: ReminderTrigger,
    val at: ZonedDateTime,
    val expense: Boolean,
    val stayMinutes: Long?,
    val placeVisits: Int,
    val placeSpendRate: Double?,
    val minutesSinceTransaction: Long?,
    val minutesSinceNotification: Long?,
    val notificationsToday: Int,
    val cashBalance: Long?,
)
