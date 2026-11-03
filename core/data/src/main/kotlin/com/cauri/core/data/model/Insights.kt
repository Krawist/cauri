package com.cauri.core.data.model

/**
 * What « Analyses » shows of a month, every figure computed by code.
 *
 * @property spent Spent in the month, in minor units of XAF.
 * @property cash Of it in cash, the rest of the counts included.
 * @property unidentified Of the cash, the rest of the counts that no expense explained.
 * @property categories Each category spent on or budgeted, the costliest first.
 * @property overrun The budget gone over the most, or else that the month will go over the most; `null` for none.
 * @property habit The costliest habit over a year, tapped 4 times at least in 30 days; `null` for none.
 */
data class Insights(
    val spent: Long,
    val cash: Long,
    val unidentified: Long,
    val categories: List<CategorySpending>,
    val overrun: BudgetOverrun?,
    val habit: HabitCost?,
)
