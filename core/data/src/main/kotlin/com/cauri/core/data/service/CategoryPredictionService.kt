package com.cauri.core.data.service

import java.time.Duration
import java.time.Instant

/**
 * Predicts the category of an operation from what the user chose before at the same merchant or the same place: the
 * categories they confirmed, their corrections of Cauri's proposals included, the recent ones weighing more.
 */
internal interface CategoryPredictionService {
    /**
     * The categories of [uses], most likely first: each use weighs half as much every [HALF_LIFE] before [now], and
     * the heaviest category comes first; ties go to the most recent.
     */
    fun predict(uses: List<CategoryUse>, now: Instant): List<Long>

    companion object {
        /** How long before a choice weighs half: a habit that changed wins within weeks. */
        val HALF_LIFE: Duration = Duration.ofDays(30)

        /** How far back the choices are read. */
        val HISTORY: Duration = Duration.ofDays(180)
    }
}
