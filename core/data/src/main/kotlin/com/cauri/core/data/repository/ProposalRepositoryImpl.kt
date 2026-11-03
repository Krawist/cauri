package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.model.ProposalSource
import com.cauri.core.data.service.ProposalService
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ProposalRepository]. */
@Singleton
internal class ProposalRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val proposals: ProposalService,
) : ProposalRepository {
    override suspend fun acceptance(source: ProposalSource): Map<String, Double> =
        proposals.acceptance(outcomes = transactionDao.getProposalOutcomes(), source = source)
}
