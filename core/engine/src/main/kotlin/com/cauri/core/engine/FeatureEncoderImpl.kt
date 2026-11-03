package com.cauri.core.engine

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin

/** Default implementation for [FeatureEncoder]. Public so that the app's Hilt modules can bind it. */
@Singleton
class FeatureEncoderImpl @Inject constructor() : FeatureEncoder {
    override fun encode(context: ReminderContext, rulesScore: Double): List<Double> {
        // The hour on a circle: 23 h is close to midnight.
        val hour = (context.at.hour + context.at.minute / MINUTES_PER_HOUR) / HOURS_PER_DAY * 2 * PI
        return listOf(
            flag(context.trigger == ReminderTrigger.VISIT_END),
            flag(context.trigger == ReminderTrigger.DETECTED_OPERATION),
            sin(hour),
            cos(hour),
            (context.at.dayOfWeek.value - 1) / LAST_DAY,
            stayBand(context.stayMinutes),
            // Unknown, a place counts halfway.
            context.placeSpendRate ?: HALF,
            minutes(context.minutesSinceTransaction),
            minutes(context.minutesSinceNotification),
            min(context.notificationsToday / MANY_NOTIFICATIONS, 1.0),
            cash(context.cashBalance),
            rulesScore,
        )
    }

    private fun flag(value: Boolean) = if (value) 1.0 else 0.0

    // Under 5 minutes, under 15, under 30, an hour, longer: from 0 to 1.
    private fun stayBand(minutes: Long?): Double {
        val stay = minutes ?: return 0.0
        val band = STAY_BANDS.indexOfFirst { stay < it }.takeIf { it >= 0 } ?: STAY_BANDS.size
        return band / STAY_BANDS.size.toDouble()
    }

    // A logarithm of the minutes, about 1 after a week; 1 when never.
    private fun minutes(value: Long?): Double = value?.let { min(ln(1.0 + it.coerceAtLeast(0)) / LN_WEEK, 1.0) } ?: 1.0

    // A logarithm of the francs, about 1 at 100 000 F; 0 when unknown or below zero.
    private fun cash(value: Long?): Double = value?.takeIf { it > 0 }?.let { min(ln(1.0 + it) / LN_CASH, 1.0) } ?: 0.0

    private companion object {
        const val MINUTES_PER_HOUR = 60.0
        const val HOURS_PER_DAY = 24.0
        const val LAST_DAY = 6.0
        const val HALF = 0.5
        const val MANY_NOTIFICATIONS = 10.0
        val STAY_BANDS = listOf(5L, 15L, 30L, 60L)
        val LN_WEEK = ln(1.0 + 7 * 24 * 60)
        val LN_CASH = ln(1.0 + 100_000)
    }
}
