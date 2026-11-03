package com.cauri.core.engine

import java.time.LocalTime

/**
 * When the evening summary comes: the time the user chose, or else one that follows when they usually answer their
 * cards in the evening (18 h to 23 h), once 10 answers there show it; 20:30 before.
 */
interface SummaryTime {
    /**
     * The time of the summary: [chosen] when the user set one; else the median of the [answers] given in the evening,
     * rounded down to the half hour and kept between 18:00 and 22:00, with 10 of them at least; else [DEFAULT].
     */
    fun at(chosen: LocalTime?, answers: List<LocalTime>): LocalTime

    companion object {
        /** Time of the summary before Cauri knows when the user answers. */
        val DEFAULT: LocalTime = LocalTime.of(20, 30)
    }
}
