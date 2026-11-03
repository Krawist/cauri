package com.cauri.core.data.model

/**
 * A cash expense found while splitting the gap of a count.
 *
 * @property amount Amount, in minor units of the Cash account's currency.
 * @property categoryId Category; `null` for the part nobody could place, shown as « Divers cash ».
 * @property note Name of the expense, such as the habit it comes from; `null` when the category says enough.
 */
data class ReconciledExpense(val amount: Long, val categoryId: Long?, val note: String?)
