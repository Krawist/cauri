package com.cauri.core.data.repository

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
     * Records a confirmed cash expense of [amount] (minor units) in [categoryId], named [note], made at [occurredAt],
     * and returns its id.
     */
    suspend fun recordCashExpense(amount: Long, categoryId: Long?, note: String, occurredAt: Instant): Long

    /** Deletes the transaction [id], such as an expense the user undid right away. */
    suspend fun deleteTransaction(id: Long)
}
