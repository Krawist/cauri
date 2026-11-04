package com.cauri.core.data.service

/**
 * The money of the month so far.
 *
 * @property received What arrived, in minor units of XAF.
 * @property savings What the splits of the incomes put aside, in the same unit.
 * @property unpaidFixedCosts What the fixed costs not paid yet cost, in the same unit.
 * @property spent What was spent, fixed costs paid included, in the same unit.
 */
internal data class MonthMoney(val received: Long, val savings: Long, val unpaidFixedCosts: Long, val spent: Long)
