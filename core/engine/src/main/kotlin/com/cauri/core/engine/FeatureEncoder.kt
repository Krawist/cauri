package com.cauri.core.engine

/**
 * The context of a decision as the 12 numbers the bandit learns from, each between -1 and 1 or about: the trigger
 * (visit, operation read), the hour as a sine and a cosine, the day of the week, the stay by bands, the expense rate of
 * the place, the time since the last transaction and since the last notification, the notifications of the day, the
 * cash in the pocket, and the score of the rules.
 */
interface FeatureEncoder {
    /** [context] as numbers, in the layout of [VERSION], [rulesScore] last. */
    fun encode(context: ReminderContext, rulesScore: Double): List<Double>

    companion object {
        /** Version of the layout, logged with each vector: a new layout gets a new version. */
        const val VERSION = 1

        /** How many numbers a vector holds. */
        const val SIZE = 12
    }
}
