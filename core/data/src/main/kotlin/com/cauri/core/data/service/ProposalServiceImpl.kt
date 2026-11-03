package com.cauri.core.data.service

import com.cauri.core.data.model.ProposalSource
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ProposalService]. */
@Singleton
internal class ProposalServiceImpl @Inject constructor() : ProposalService {
    override fun acceptance(outcomes: List<ProposalOutcome>, source: ProposalSource): Map<String, Double> = outcomes
        .mapNotNull { outcome -> outcome.key(source)?.let { it to outcome.unchanged() } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, kept) -> kept.count { it }.toDouble() / kept.size }

    private fun ProposalOutcome.key(source: ProposalSource): String? = when (source) {
        ProposalSource.PLACE -> placeId?.toString()
        ProposalSource.MERCHANT -> counterparty
        ProposalSource.TEMPLATE -> templateVersion
    }

    // A part Cauri did not propose cannot be changed from its proposal.
    private fun ProposalOutcome.unchanged() = (proposedCategoryId == null || proposedCategoryId == categoryId) &&
        (proposedAmount == null || proposedAmount == amount)
}
