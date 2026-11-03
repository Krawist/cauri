package com.cauri.core.data.repository

import com.cauri.core.data.model.PendingOperation

/** The user's answers to the operations Cauri read in messages (« À confirmer »). */
interface ConfirmationRepository {
    /**
     * Confirms [operation] with [categoryId] and [amount] (minor units), keeping [proposedCategoryId], Cauri's
     * proposal, next to them; a transfer is confirmed on both sides. Its reminder decisions close as confirmed.
     */
    suspend fun confirm(operation: PendingOperation, categoryId: Long?, proposedCategoryId: Long?, amount: Long)

    /**
     * Sets [operation] aside: never counted. An expense closes its reminder decisions as « Rien dépensé », any other
     * operation as dismissed.
     */
    suspend fun dismiss(operation: PendingOperation)

    /** Undoes the answer to the operation [transactionId]: it waits for the user again, as read in its message. */
    suspend fun reopen(transactionId: Long)
}
