package com.cauri.core.data.repository

import java.time.Instant

/** The transfers between the user's accounts: typed by the user, or found by Cauri in the operations detected. */
interface TransferRepository {
    /**
     * Records the transfer of [amount] (minor units) from [fromAccountId] to [toAccountId] at [occurredAt], typed by
     * the user: two confirmed transactions sharing a transfer group, all or none. Cash to another account is a deposit,
     * an account to Cash a withdrawal.
     */
    suspend fun recordTransfer(fromAccountId: Long, toAccountId: Long, amount: Long, occurredAt: Instant)

    /**
     * Links the detected debits and credits that are one transfer between two of the user's accounts, such as UBA to
     * Orange Money, so that they count neither as spending nor as income, and returns how many it linked. Safe to call
     * after each signal: an operation is linked once, and never again once the user undid its link.
     */
    suspend fun linkTransfers(): Int

    /** Undoes the link [linkId]: the debit and the credit count apart again. Does nothing when it is already undone. */
    suspend fun unlinkTransfer(linkId: Long)
}
