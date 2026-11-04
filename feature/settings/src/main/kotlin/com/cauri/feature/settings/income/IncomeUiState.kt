package com.cauri.feature.settings.income

import com.cauri.core.data.model.FixedCost
import com.cauri.core.data.model.FixedCostSuggestion
import com.cauri.core.data.model.IncomeSplitSettings

/**
 * « Répartir mes revenus ».
 *
 * @property settings The savings rate and the smallest income split.
 * @property fixedCosts The fixed costs, by day of the month.
 * @property suggestions The expenses paid every month Cauri asks about.
 * @property draft The fixed cost in its sheet; `null` while it is closed.
 */
internal data class IncomeUiState(
    val settings: IncomeSplitSettings = IncomeSplitSettings(savingsRate = 10, threshold = 10_000),
    val fixedCosts: List<FixedCost> = emptyList(),
    val suggestions: List<FixedCostSuggestion> = emptyList(),
    val draft: FixedCostDraft? = null,
)
