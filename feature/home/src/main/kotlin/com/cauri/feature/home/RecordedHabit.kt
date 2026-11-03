package com.cauri.feature.home

/**
 * A cash expense just recorded with a tap on a habit, which the user can still undo.
 *
 * @property transactionId Expense recorded.
 * @property name Name of the habit.
 * @property amount Amount recorded, in FCFA.
 */
internal data class RecordedHabit(val transactionId: Long, val name: String, val amount: Long)
