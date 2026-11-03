package com.cauri.core.data.model

/**
 * The monthly budget of a category.
 *
 * @property categoryId Category budgeted.
 * @property categoryName Its name.
 * @property amount Amount a month, in minor units of XAF.
 */
data class Budget(val categoryId: Long, val categoryName: String, val amount: Long)
