package com.cauri.core.engine

import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [SummaryTime]. Public so that the app's Hilt modules can bind it. */
@Singleton
class SummaryTimeImpl @Inject constructor() : SummaryTime {
    override fun at(chosen: LocalTime?, answers: List<LocalTime>): LocalTime {
        val evening = answers.filter { !it.isBefore(EVENING_START) && it.isBefore(EVENING_END) }.sorted()
        return when {
            chosen != null -> chosen
            evening.size < KNOWN_ANSWERS -> SummaryTime.DEFAULT
            else -> {
                val median = evening[evening.size / 2].toSecondOfDay() / SECONDS_PER_MINUTE
                LocalTime.ofSecondOfDay((median - median % STEP_MINUTES) * SECONDS_PER_MINUTE)
                    .coerceIn(EVENING_START, LATEST)
            }
        }
    }

    private companion object {
        val EVENING_START: LocalTime = LocalTime.of(18, 0)
        val EVENING_END: LocalTime = LocalTime.of(23, 0)
        val LATEST: LocalTime = LocalTime.of(22, 0)
        const val KNOWN_ANSWERS = 10
        const val STEP_MINUTES = 30L
        const val SECONDS_PER_MINUTE = 60L
    }
}
