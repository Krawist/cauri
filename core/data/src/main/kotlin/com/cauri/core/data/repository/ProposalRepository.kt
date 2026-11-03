package com.cauri.core.data.repository

import com.cauri.core.data.model.ProposalSource

/** How Cauri's proposals fare with the user, the measure of the predictions (CAU-42). */
interface ProposalRepository {
    /**
     * The share, from 0 to 1, of Cauri's proposals the user confirmed unchanged, per value of [source]: the place id,
     * the merchant or the template.
     */
    suspend fun acceptance(source: ProposalSource): Map<String, Double>
}
