package com.cauri.core.data.model

/**
 * How the user wants their incomes split.
 *
 * @property savingsRate Share of each income put aside, in percent, from 0 to 50.
 * @property threshold Smallest income Cauri offers to split, in minor units of XAF: smaller ones go to the budgets.
 */
data class IncomeSplitSettings(val savingsRate: Int, val threshold: Long)
