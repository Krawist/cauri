package com.cauri.feature.home

import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.Habit

/**
 * What the home screen shows.
 *
 * @property initial First letter of the user's name, in capitals, on the profile button; empty until it is read.
 * @property cash Estimated cash in pocket; `null` until it is read, or before the first count.
 * @property habits One-tap cash expenses, in the order of the row.
 * @property categories Spending categories a habit can record into, by name.
 * @property recorded Expense just recorded with a tap, while it can be undone; `null` otherwise.
 * @property editing Habit open in the habit sheet; `null` when the sheet is closed.
 * @property managing Whether the list to reorder the habits is open.
 */
internal data class HomeUiState(
    val initial: String = "",
    val cash: CashBalance? = null,
    val habits: List<Habit> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recorded: RecordedHabit? = null,
    val editing: HabitDraft? = null,
    val managing: Boolean = false,
)
