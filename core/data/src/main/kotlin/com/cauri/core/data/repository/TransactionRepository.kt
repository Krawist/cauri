package com.cauri.core.data.repository

import com.cauri.core.data.model.Withdrawal

/** The money moving on the user's accounts. */
interface TransactionRepository {
    /**
     * Records [withdrawal] as a transfer from its account to the Cash account, two transactions sharing a transfer
     * group, and its fee, when there is one, as a separate expense on its account. All or nothing: it fails when the
     * withdrawal's signal already created a transaction. Parsed from a signal, it waits for the user's confirmation;
     * entered by the user, it is confirmed.
     */
    suspend fun recordWithdrawal(withdrawal: Withdrawal)
}
