package com.cauri.core.data.model

/**
 * What a habit costs over a year, at the pace of the last 30 days.
 *
 * @property name Name of the habit.
 * @property uses Taps on it over the last 30 days.
 * @property yearly Its cost over a year, in minor units of XAF, rounded to 1 000 F.
 */
data class HabitCost(val name: String, val uses: Int, val yearly: Long)
