package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity

/**
 * Finds a debit and a credit on two of the user's accounts that are one transfer, such as UBA to Orange Money, so that
 * it never counts as spending nor as income.
 */
internal interface TransferLinkService {
    /**
     * The (debit, credit) pairs among [transactions] that are one transfer: on two accounts, within 30 minutes, the
     * credit equal to the debit or smaller by at most 3 %, the operator having kept a fee. An expense or an income is
     * in one pair at most; the same amount and then the closest in time win.
     */
    fun pairs(transactions: List<TransactionEntity>): List<Pair<TransactionEntity, TransactionEntity>>

    /** [debit] and [credit] as the two sides of the transfer [group], the gap between them as a fee on the debit. */
    fun linked(debit: TransactionEntity, credit: TransactionEntity, group: String): LinkedTransfer
}
