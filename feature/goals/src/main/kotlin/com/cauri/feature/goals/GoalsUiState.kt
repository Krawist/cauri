package com.cauri.feature.goals

import com.cauri.core.data.model.Category
import com.cauri.core.data.model.GoalsOverview
import java.time.YearMonth

/**
 * « Objectifs ».
 *
 * @property month The current month, in « Mis de côté en novembre ».
 * @property overview What was put aside this month and where each goal stands; `null` until it is read.
 * @property categories The spending categories an envelope can keep money for, by name.
 * @property draft The goal in its sheet; `null` while it is closed.
 */
internal data class GoalsUiState(
    val month: YearMonth,
    val overview: GoalsOverview? = null,
    val categories: List<Category> = emptyList(),
    val draft: GoalDraft? = null,
)
