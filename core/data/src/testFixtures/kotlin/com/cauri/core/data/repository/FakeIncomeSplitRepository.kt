package com.cauri.core.data.repository

import com.cauri.core.data.model.IncomeSplit
import com.cauri.core.data.model.IncomeSplitProposal
import com.cauri.core.data.model.PendingIncome
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [IncomeSplitRepository] for tests: the test sets the income waiting; the proposal puts [savingsRate]
 * percent aside and the rest to the budgets; an answer clears the income.
 */
class FakeIncomeSplitRepository : IncomeSplitRepository {
    override val pending = MutableStateFlow<PendingIncome?>(null)

    val splits = mutableListOf<Pair<Long, IncomeSplit>>()

    val dismissed = mutableListOf<Long>()

    override suspend fun proposal(income: PendingIncome, savingsRate: Int): IncomeSplitProposal {
        val savings = income.amount * savingsRate / 100
        return IncomeSplitProposal(
            income = income,
            savingsRate = savingsRate,
            split = IncomeSplit(savings = savings, fixedCosts = 0, budgets = income.amount - savings),
            fixedCosts = emptyList(),
        )
    }

    override suspend fun split(transactionId: Long, split: IncomeSplit) {
        splits += transactionId to split
        pending.value = null
    }

    override suspend fun dismiss(transactionId: Long) {
        dismissed += transactionId
        pending.value = null
    }
}
