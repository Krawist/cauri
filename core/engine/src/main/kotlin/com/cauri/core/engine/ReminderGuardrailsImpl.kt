package com.cauri.core.engine

import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ReminderGuardrails]. Public so that the app's Hilt modules can bind it. */
@Singleton
class ReminderGuardrailsImpl @Inject constructor() : ReminderGuardrails {
    override fun allowed(context: ReminderContext, limits: ReminderLimits): Set<ReminderAction> = when {
        context.trigger == ReminderTrigger.VISIT_END && !limits.visitReminders -> setOf(ReminderAction.SKIP)
        quiet(context.at.hour, limits) ||
            context.notificationsToday >= limits.dailyCap ||
            (context.minutesSinceNotification ?: Long.MAX_VALUE) < SPACING_MINUTES ||
            context.atHomeOrWork -> setOf(ReminderAction.EVENING_SUMMARY, ReminderAction.SKIP)
        else -> ReminderAction.entries.toSet()
    }

    override fun restrict(action: ReminderAction, allowed: Set<ReminderAction>): ReminderAction = when {
        action in allowed -> action
        action == ReminderAction.NOTIFY_NOW && ReminderAction.EVENING_SUMMARY in allowed ->
            ReminderAction.EVENING_SUMMARY
        else -> ReminderAction.SKIP
    }

    // The quiet hours may go over midnight (22 h to 7 h); the same hour twice means none.
    private fun quiet(hour: Int, limits: ReminderLimits) = when {
        limits.quietFrom == limits.quietUntil -> false
        limits.quietFrom < limits.quietUntil -> hour >= limits.quietFrom && hour < limits.quietUntil
        else -> hour >= limits.quietFrom || hour < limits.quietUntil
    }

    private companion object {
        const val SPACING_MINUTES = 30L
    }
}
