package com.cauri.feature.settings.categories

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import java.time.YearMonth

/**
 * « Catégories ».
 *
 * @property month The current month, in « Novembre · dépenses du mois ».
 * @property nature The tab shown: spending, income or savings.
 * @property categories The active categories, sub-categories included, in the user's order.
 * @property archived The archived categories, by name.
 * @property totals What each category counted this month, in FCFA, by category (a sub-category apart).
 * @property expanded The categories opened to their sub-categories.
 * @property draft The category in its sheet; `null` while it is closed.
 * @property merging Whether the list of the categories to merge the draft into is open.
 * @property showingArchived Whether the archived categories are shown.
 */
internal data class CategoriesUiState(
    val month: YearMonth,
    val nature: CategoryNature = CategoryNature.EXPENSE,
    val categories: List<Category> = emptyList(),
    val archived: List<Category> = emptyList(),
    val totals: Map<Long, Long> = emptyMap(),
    val expanded: Set<Long> = emptySet(),
    val draft: CategoryDraft? = null,
    val merging: Boolean = false,
    val showingArchived: Boolean = false,
)
