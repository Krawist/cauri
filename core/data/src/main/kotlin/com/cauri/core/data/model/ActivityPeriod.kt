package com.cauri.core.data.model

import java.time.LocalDate

/** The days « Activité » shows. */
sealed interface ActivityPeriod {
    /** The whole history: the totals are then those of the current month. */
    data object All : ActivityPeriod

    /** Since Monday. */
    data object ThisWeek : ActivityPeriod

    /** Since the first of the month. */
    data object ThisMonth : ActivityPeriod

    /** The whole previous month. */
    data object LastMonth : ActivityPeriod

    /**
     * Two days the user chose.
     *
     * @property from First day shown.
     * @property to Last day shown, [from] included when the same.
     */
    data class Between(val from: LocalDate, val to: LocalDate) : ActivityPeriod
}
