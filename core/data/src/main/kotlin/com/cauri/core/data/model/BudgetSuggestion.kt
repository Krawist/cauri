package com.cauri.core.data.model

/**
 * A budget Cauri suggests for a category from what was really spent there.
 *
 * @property categoryId Category.
 * @property categoryName Its name.
 * @property amount Amount a month suggested, in minor units of XAF, rounded up to 500 F.
 */
data class BudgetSuggestion(val categoryId: Long, val categoryName: String, val amount: Long)
