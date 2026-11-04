package com.cauri.core.data.model

/**
 * How an income is split, savings first.
 *
 * @property savings Put aside, in minor units of XAF.
 * @property fixedCosts Kept for the fixed costs of the month not paid yet, in the same unit.
 * @property budgets Left for the budgets and the spending, in the same unit.
 */
data class IncomeSplit(val savings: Long, val fixedCosts: Long, val budgets: Long)
