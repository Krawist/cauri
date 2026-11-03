package com.cauri.core.data.service

/**
 * A cash expense recorded by a tap on a habit, named after it.
 *
 * @property note Name of the habit.
 * @property amount Amount, in minor units of XAF.
 */
internal data class HabitUse(val note: String, val amount: Long)
