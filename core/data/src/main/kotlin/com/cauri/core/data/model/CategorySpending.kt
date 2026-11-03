package com.cauri.core.data.model

/**
 * What a category cost in the month, against its budget.
 *
 * @property name Name of the category.
 * @property spent Spent in the month, in minor units of XAF.
 * @property budget Its budget a month, in the same unit; `null` without budget.
 */
data class CategorySpending(val name: String, val spent: Long, val budget: Long?)
