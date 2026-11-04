package com.cauri.core.data.service

/**
 * The fixed costs of the month, as an income is split.
 *
 * @property unpaid What the fixed costs not paid yet cost, in minor units of XAF.
 * @property paid What those paid this month cost, in the same unit.
 * @property kept What earlier incomes of the month kept for the fixed costs, in the same unit.
 */
internal data class FixedCostTotals(val unpaid: Long, val paid: Long, val kept: Long)
