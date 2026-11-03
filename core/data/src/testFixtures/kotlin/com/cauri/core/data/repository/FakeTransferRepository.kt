package com.cauri.core.data.repository

import java.time.Instant

/** In-memory [TransferRepository] for tests: it keeps the transfers typed, counts the calls and links nothing. */
class FakeTransferRepository : TransferRepository {
    /**
     * A transfer recorded by [recordTransfer].
     *
     * @property fromAccountId Account the money left.
     * @property toAccountId Account the money arrived on.
     * @property amount Amount, in minor units.
     * @property occurredAt When it moved.
     */
    data class Transfer(val fromAccountId: Long, val toAccountId: Long, val amount: Long, val occurredAt: Instant)

    val transfers = mutableListOf<Transfer>()

    override suspend fun recordTransfer(fromAccountId: Long, toAccountId: Long, amount: Long, occurredAt: Instant) {
        transfers += Transfer(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            occurredAt = occurredAt,
        )
    }

    /** How many times [linkTransfers] ran. */
    var linkRuns = 0
    val unlinked = mutableListOf<Long>()

    override suspend fun linkTransfers(): Int {
        linkRuns++
        return 0
    }

    override suspend fun unlinkTransfer(linkId: Long) {
        unlinked += linkId
    }
}
