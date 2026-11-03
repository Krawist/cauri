package com.cauri.core.data.service

/**
 * An expense counted in the month, as the insights read it.
 *
 * @property categoryId Its category; `null` for none.
 * @property amount Amount, in minor units of XAF.
 * @property cash Whether it was paid from the Cash account.
 * @property unidentified Whether it is the rest of a cash count that no expense explained.
 */
internal data class MonthExpense(val categoryId: Long?, val amount: Long, val cash: Boolean, val unidentified: Boolean)
