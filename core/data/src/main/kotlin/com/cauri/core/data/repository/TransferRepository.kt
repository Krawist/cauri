package com.cauri.core.data.repository

/** The transfers between the user's accounts that Cauri finds in the operations it detected. */
interface TransferRepository {
    /**
     * Links the detected debits and credits that are one transfer between two of the user's accounts, such as UBA to
     * Orange Money, so that they count neither as spending nor as income, and returns how many it linked. Safe to call
     * after each signal: an operation is linked once, and never again once the user undid its link.
     */
    suspend fun linkTransfers(): Int

    /** Undoes the link [linkId]: the debit and the credit count apart again. Does nothing when it is already undone. */
    suspend fun unlinkTransfer(linkId: Long)
}
