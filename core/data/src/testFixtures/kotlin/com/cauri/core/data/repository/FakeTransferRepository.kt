package com.cauri.core.data.repository

/** In-memory [TransferRepository] for tests: it counts the calls and links nothing. */
class FakeTransferRepository : TransferRepository {
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
