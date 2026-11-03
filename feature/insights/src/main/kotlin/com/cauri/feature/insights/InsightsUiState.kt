package com.cauri.feature.insights

import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.data.model.Insights
import java.time.YearMonth

/**
 * What « Analyses » shows.
 *
 * @property suggestions The budgets Cauri suggests, one card each, until accepted, adjusted or ignored.
 * @property adjusting Suggestion whose amount the user is typing; `null` when the field is closed.
 * @property month Month the insights are about.
 * @property insights The figures of [month]; `null` until read.
 */
internal data class InsightsUiState(
    val suggestions: List<BudgetSuggestion> = emptyList(),
    val adjusting: BudgetSuggestion? = null,
    val month: YearMonth,
    val insights: Insights? = null,
)
