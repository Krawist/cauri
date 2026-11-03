package com.cauri.core.engine

import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ReminderRules]. Public so that the app's Hilt modules can bind it. */
@Singleton
class ReminderRulesImpl @Inject constructor() : ReminderRules {
    override fun decide(context: ReminderContext): ReminderAction {
        val wanted = when (context.trigger) {
            ReminderTrigger.VISIT_END -> visit(context)
            ReminderTrigger.DETECTED_OPERATION ->
                if (context.expense) ReminderAction.NOTIFY_NOW else ReminderAction.EVENING_SUMMARY
            ReminderTrigger.WITHDRAWAL -> ReminderAction.EVENING_SUMMARY
        }
        val quiet = context.at.hour >= NIGHT_START || context.at.hour < NIGHT_END
        val recent = (context.minutesSinceNotification ?: Long.MAX_VALUE) < SPACING_MINUTES
        return if (wanted == ReminderAction.NOTIFY_NOW && (quiet || recent)) ReminderAction.EVENING_SUMMARY else wanted
    }

    override fun score(action: ReminderAction): Double = when (action) {
        ReminderAction.NOTIFY_NOW -> 1.0
        ReminderAction.EVENING_SUMMARY -> HALF
        ReminderAction.SKIP -> 0.0
    }

    // A long stay at a place that leads to an expense asks at once; a place that rarely does is left alone.
    private fun visit(context: ReminderContext): ReminderAction {
        val rate = context.placeSpendRate.takeIf { context.placeVisits >= KNOWN_VISITS }
        return when {
            rate != null && rate < RARE -> ReminderAction.SKIP
            (context.stayMinutes ?: 0) < LONG_STAY_MINUTES -> ReminderAction.EVENING_SUMMARY
            rate == null || rate >= OFTEN -> ReminderAction.NOTIFY_NOW
            else -> ReminderAction.EVENING_SUMMARY
        }
    }

    private companion object {
        const val NIGHT_START = 22
        const val NIGHT_END = 7
        const val SPACING_MINUTES = 30L
        const val LONG_STAY_MINUTES = 10L
        const val KNOWN_VISITS = 5
        const val OFTEN = 0.5
        const val RARE = 0.2
        const val HALF = 0.5
    }
}
