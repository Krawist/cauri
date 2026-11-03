package com.cauri.core.engine

/**
 * The guardrails of the reminders, applied before the rules or the bandit decide: never a notification in the quiet
 * hours, past the daily cap, within 30 minutes of the last one, nor while the user is at home or at work (the evening
 * summary instead); no reminder at all after a visit when the user turned them off.
 */
interface ReminderGuardrails {
    /** The actions allowed in [context] under [limits]; [ReminderAction.SKIP] always is. */
    fun allowed(context: ReminderContext, limits: ReminderLimits): Set<ReminderAction>

    /** [action] when [allowed], else the closest allowed one: the evening summary for a notification, else skip. */
    fun restrict(action: ReminderAction, allowed: Set<ReminderAction>): ReminderAction
}
