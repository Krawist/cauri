package com.cauri.core.data.model

/**
 * The split Cauri proposes for an income arrived.
 *
 * @property income The income.
 * @property savingsRate Share put aside, in percent.
 * @property split What goes where.
 * @property fixedCosts The fixed costs of the month not paid yet, which [IncomeSplit.fixedCosts] keeps money for,
 *   minus what earlier incomes of the month kept already.
 */
data class IncomeSplitProposal(
    val income: PendingIncome,
    val savingsRate: Int,
    val split: IncomeSplit,
    val fixedCosts: List<FixedCost>,
)
