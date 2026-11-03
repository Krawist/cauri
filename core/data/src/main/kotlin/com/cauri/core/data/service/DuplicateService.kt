package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import java.time.Duration

/**
 * Finds a manual entry and a detected transaction that are the same operation, such as a payment typed by the user and
 * then reported by Orange Money, and merges them so that it counts once.
 */
internal interface DuplicateService {
    /**
     * Among [candidates], the transaction that is the same operation as [transaction]: on the same account, of the same
     * type and amount, within two hours, one typed by the user and the other read in a signal and not confirmed yet.
     * The closest in time when several match; `null` when none does. Transfers are left to their own linking.
     */
    fun sameOperation(transaction: TransactionEntity, candidates: List<TransactionEntity>): TransactionEntity?

    /**
     * [detected] with the values of the user's [manual] entry: its category, note and place win, the message fills in
     * the rest, and the operation is confirmed.
     */
    fun merged(detected: TransactionEntity, manual: TransactionEntity): TransactionEntity

    companion object {
        /** Largest gap between the two sides of the same operation. */
        val WINDOW: Duration = Duration.ofHours(2)
    }
}
