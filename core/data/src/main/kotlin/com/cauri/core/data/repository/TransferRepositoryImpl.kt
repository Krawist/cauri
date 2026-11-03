package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.TransferLinkDao
import com.cauri.core.data.database.entity.TransferLinkEntity
import com.cauri.core.data.service.TransferLinkService
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [TransferRepository]. */
@Singleton
internal class TransferRepositoryImpl @Inject constructor(
    private val transferLinkDao: TransferLinkDao,
    private val transferLinks: TransferLinkService,
    private val clock: Clock,
) : TransferRepository {
    override suspend fun linkTransfers(): Int {
        val pairs = transferLinks.pairs(transferLinkDao.getLinkable())
        pairs.forEach { (debit, credit) ->
            val transfer = transferLinks.linked(debit = debit, credit = credit, group = UUID.randomUUID().toString())
            transferLinkDao.link(
                debit = transfer.debit,
                credit = transfer.credit,
                fee = transfer.fee,
                // The fee gets its id when inserted: the DAO fills it in.
                link = TransferLinkEntity(
                    debitId = debit.id,
                    creditId = credit.id,
                    feeId = null,
                    debitAmount = debit.amount,
                    linkedAt = clock.instant(),
                    undoneAt = null,
                ),
            )
        }
        return pairs.size
    }

    override suspend fun unlinkTransfer(linkId: Long) {
        transferLinkDao.getLink(linkId)
            ?.takeIf { it.undoneAt == null }
            ?.let { transferLinkDao.unlink(link = it, undoneAt = clock.instant()) }
    }
}
