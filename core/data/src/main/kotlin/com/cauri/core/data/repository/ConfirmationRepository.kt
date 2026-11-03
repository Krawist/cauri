package com.cauri.core.data.repository

import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.TransactionEdit

/** The user's answers to the operations Cauri read in messages (« À confirmer »). */
interface ConfirmationRepository {
    /**
     * Confirms [operation] with [categoryId] and [amount] (minor units), keeping [proposedCategoryId], Cauri's
     * proposal, next to them; a transfer is confirmed on both sides. Its reminder decisions close as confirmed.
     */
    suspend fun confirm(operation: PendingOperation, categoryId: Long?, proposedCategoryId: Long?, amount: Long)

    /**
     * Confirms the transaction of [edit] as the user kept it in the edit sheet, all or none: each part becomes a
     * transaction linked to it, which keeps the rest of the amount; a transfer is confirmed on both sides. The account
     * of an operation read in a message, or of a transfer, is kept. Its reminder decisions close as confirmed.
     */
    suspend fun confirm(edit: TransactionEdit)

    /**
     * Sets the transaction [transactionId], of [type], aside: never counted. An expense closes its reminder decisions
     * as « Rien dépensé », any other operation as dismissed.
     */
    suspend fun dismiss(transactionId: Long, type: PendingType)

    /** Undoes the answer to the operation [transactionId]: it waits for the user again, as read in its message. */
    suspend fun reopen(transactionId: Long)
}
