package com.cauri.feature.insights

import com.cauri.core.data.model.BudgetSuggestion

/**
 * What « Analyses » shows.
 *
 * @property suggestions The budgets Cauri suggests, one card each, until accepted, adjusted or ignored.
 * @property adjusting Suggestion whose amount the user is typing; `null` when the field is closed.
 */
internal data class InsightsUiState(
    val suggestions: List<BudgetSuggestion> = emptyList(),
    val adjusting: BudgetSuggestion? = null,
)
