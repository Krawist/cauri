package com.cauri.feature.cash

/**
 * A line of the gap split: a moment Cauri noticed or an expense the user added, with the amount typed for it.
 *
 * @property key Stable identifier of the line on the screen.
 * @property name Name shown: the habit's or the category's.
 * @property fromHabit Whether Cauri suggested it from a habit, rather than the user adding it.
 * @property hint Usual amount, in FCFA, shown grey until something is typed; `null` when there is none.
 * @property categoryId Category of the expense it records; `null` when none.
 * @property note Name given to the expense it records; `null` when its category says enough.
 * @property amount Amount typed, digits only, in FCFA; empty while nothing is typed, and then it counts nothing.
 */
internal data class GapLine(
    val key: String,
    val name: String,
    val fromHabit: Boolean,
    val hint: Long?,
    val categoryId: Long?,
    val note: String?,
    val amount: String = "",
)
