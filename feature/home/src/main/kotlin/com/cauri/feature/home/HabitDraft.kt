package com.cauri.feature.home

/**
 * A habit being added or edited in the habit sheet.
 *
 * @property id Habit being edited; `null` for a new one.
 * @property name Name as typed.
 * @property amount Amount as typed, digits only, in FCFA; empty when not given yet.
 * @property categoryId Chosen category; `null` when none.
 * @property nameMissing Whether the user tried to save without a name.
 * @property amountMissing Whether the user tried to save without an amount above 0.
 */
internal data class HabitDraft(
    val id: Long? = null,
    val name: String = "",
    val amount: String = "",
    val categoryId: Long? = null,
    val nameMissing: Boolean = false,
    val amountMissing: Boolean = false,
)
