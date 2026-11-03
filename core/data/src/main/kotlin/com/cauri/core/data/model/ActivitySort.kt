package com.cauri.core.data.model

/** The order of the lines of « Activité ». */
enum class ActivitySort {
    /** Newest first, under the headers of their days. */
    DATE,

    /** Largest amount first, without days. */
    AMOUNT,
}
