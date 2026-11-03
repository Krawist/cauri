package com.cauri.core.data.repository

import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.EditableTransaction
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.model.RecordedEntry
import com.cauri.core.data.model.Withdrawal
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow

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

    override val pending = MutableStateFlow(emptyList<PendingOperation>())

    /** Transactions [getEditable] answers, by id. */
    val editable = mutableMapOf<Long, EditableTransaction>()

    override suspend fun getEditable(transactionId: Long) = editable[transactionId]

    val withdrawals = mutableListOf<Withdrawal>()
    val detected = mutableListOf<DetectedOperation>()
    val cashExpenses = mutableListOf<CashExpense>()

    /**
     * A split of a gap recorded by [recordReconciliation].
     *
     * @property accountId Account of the expenses.
     * @property expenses Expenses recorded.
     * @property at Time of the count or of the balance typed.
     */
    data class Reconciliation(val accountId: Long, val expenses: List<ReconciledExpense>, val at: Instant)

    val reconciliations = mutableListOf<Reconciliation>()
    private var nextId = 1L

    override suspend fun recordWithdrawal(withdrawal: Withdrawal) {
        withdrawals += withdrawal
    }

    override suspend fun recordDetected(operation: DetectedOperation) {
        detected += operation
    }

    val entries = mutableListOf<ManualEntry>()
    val undoneMerges = mutableListOf<Long>()

    /** Merge [recordEntry] answers, as if Cauri had detected the same operation; `null` for none. */
    var mergeId: Long? = null

    override suspend fun recordEntry(entry: ManualEntry): RecordedEntry {
        entries += entry
        return RecordedEntry(transactionId = nextId++, mergeId = mergeId)
    }

    override suspend fun undoMerge(mergeId: Long) {
        undoneMerges += mergeId
    }

    override suspend fun recordCashExpense(amount: Long, categoryId: Long?, note: String, occurredAt: Instant): Long {
        val id = nextId++
        cashExpenses +=
            CashExpense(id = id, amount = amount, categoryId = categoryId, note = note, occurredAt = occurredAt)
        return id
    }

    override suspend fun recordReconciliation(accountId: Long, expenses: List<ReconciledExpense>, at: Instant) {
        reconciliations += Reconciliation(accountId = accountId, expenses = expenses, at = at)
    }

    /** What [hasCashWithdrawalSince] answers. */
    var cashWithdrawn = false

    override suspend fun hasCashWithdrawalSince(since: Instant) = cashWithdrawn

    override suspend fun deleteTransaction(id: Long) {
        cashExpenses.removeAll { it.id == id }
    }
}
