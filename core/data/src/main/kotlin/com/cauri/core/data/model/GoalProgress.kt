package com.cauri.core.data.model

import java.time.YearMonth

/**
 * Where a goal stands.
 *
 * @property goal The goal.
 * @property saved In minor units of XAF: what was put aside for a saving, what was spent this month in the category of
 *   an envelope; 0 for a tontine.
 * @property estimate Month a saving should be reached in, from a cautious estimate of the savings to come; `null` once
 *   reached, before a month of income, and for the other kinds.
 * @property receiving Whether the savings of the next incomes go to it: the first saving not reached.
 */
data class GoalProgress(val goal: Goal, val saved: Long, val estimate: YearMonth?, val receiving: Boolean)
