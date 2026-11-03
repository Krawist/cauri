package com.cauri.core.data.repository

import com.cauri.core.data.model.DetectedOperation
import com.cauri.core.data.model.ManualEntry
import com.cauri.core.data.model.ReconciledExpense
import com.cauri.core.data.model.RecordedEntry
import com.cauri.core.data.model.Withdrawal
import java.time.Instant

/** The money moving on the user's accounts. */
interface TransactionRepository {
    /**
     * Records [withdrawal] as a transfer from its account to the Cash account, two transactions sharing a transfer
     * group, and its fee, when there is one, as a separate expense on its account. All or nothing: it fails when the
     * withdrawal's signal already created a transaction. Parsed from a signal, it waits for the user's confirmation;
     * entered by the user, it is confirmed.
     */
    suspend fun recordWithdrawal(withdrawal: Withdrawal)

    /**
     * Records [operation], read in a raw signal, as transactions waiting for the user's confirmation, and marks the
     * signal read. A withdrawal is a transfer to the Cash account and a deposit a transfer from it; a fee is a separate
     * expense on the account. An expense or an income the user already typed within two hours, on the same account
     * and of the same amount, is merged into it instead of counting twice. All or nothing.
     */
    suspend fun recordDetected(operation: DetectedOperation)

    /**
     * Records [entry], typed by the user, as a confirmed transaction; when Cauri already detected the same operation
     * (same account, type and amount, within two hours), the entry is merged into it instead: the user's category and
     * note win, the message fills in the rest.
     */
    suspend fun recordEntry(entry: ManualEntry): RecordedEntry

    /** Undoes the merge [mergeId]: the two transactions count apart again. Does nothing when it is already undone. */
    suspend fun undoMerge(mergeId: Long)

    /**
     * Records a confirmed cash expense of [amount] (minor units) in [categoryId], named [note], made at [occurredAt],
     * and returns its id.
     */
    suspend fun recordCashExpense(amount: Long, categoryId: Long?, note: String, occurredAt: Instant): Long

    /**
     * Records [expenses], the split of a count's gap, as confirmed cash expenses made at [countedAt], the time of the
     * count, which already takes them into account. All or nothing.
     */
    suspend fun recordReconciliation(expenses: List<ReconciledExpense>, countedAt: Instant)

    /** Whether cash was withdrawn after [since], a withdrawal « Rien dépensé » left aside. */
    suspend fun hasCashWithdrawalSince(since: Instant): Boolean

    /** Deletes the transaction [id], such as an expense the user undid right away. */
    suspend fun deleteTransaction(id: Long)
}
