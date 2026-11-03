package com.cauri.core.data.repository

import com.cauri.core.data.model.Withdrawal
import java.time.Instant

/** In-memory [TransactionRepository] for tests: recorded cash expenses get the ids 1, 2, 3… in order. */
class FakeTransactionRepository : TransactionRepository {
    /**
     * A cash expense recorded by [recordCashExpense].
     *
     * @property id Id it was given.
     * @property amount Amount, in minor units.
     * @property categoryId Category; `null` when none.
     * @property note Name of the expense.
     * @property occurredAt When it was made.
     */
    data class CashExpense(
        val id: Long,
        val amount: Long,
        val categoryId: Long?,
        val note: String,
        val occurredAt: Instant,
    )

    val withdrawals = mutableListOf<Withdrawal>()
    val cashExpenses = mutableListOf<CashExpense>()
    private var nextId = 1L

    override suspend fun recordWithdrawal(withdrawal: Withdrawal) {
        withdrawals += withdrawal
    }

    override suspend fun recordCashExpense(amount: Long, categoryId: Long?, note: String, occurredAt: Instant): Long {
        val id = nextId++
        cashExpenses +=
            CashExpense(id = id, amount = amount, categoryId = categoryId, note = note, occurredAt = occurredAt)
        return id
    }

    override suspend fun deleteTransaction(id: Long) {
        cashExpenses.removeAll { it.id == id }
    }
}
