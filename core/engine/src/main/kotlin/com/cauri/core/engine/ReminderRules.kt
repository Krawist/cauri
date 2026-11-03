package com.cauri.core.engine

/** The starting rules of the reminder engine, until the bandit learns enough to take over (CAU-41). */
interface ReminderRules {
    /**
     * The action of the rules in [context]. Never a notification at night (22 h to 7 h) nor within 30 minutes of the
     * last one: the evening summary instead. A visit notifies after a stay of 10 minutes at least at a place that leads
     * to an expense half the time or more, or without enough history (5 visits); a place below a fifth is skipped, a
     * shorter stay or a place in between goes to the summary. An expense read in a message notifies; a withdrawal, an
     * income or a transfer goes to the summary.
     */
    fun decide(context: ReminderContext): ReminderAction

    /** The rules' score of [action], from 0 to 1, a signal of the bandit: how strongly they would ask. */
    fun score(action: ReminderAction): Double

    companion object {
        /** Version of the rules, logged with each of their decisions. */
        const val VERSION = "rules-1"
    }
}
