package com.cauri.core.data.service

import com.cauri.core.data.model.ProposalSource

/** Measures Cauri's proposals: how often the user confirms them unchanged. */
internal interface ProposalService {
    /**
     * The share, from 0 to 1, of the [outcomes] whose proposal was confirmed unchanged (the category and the amount
     * proposed both kept), per value of [source]: the place id, the merchant or the template. Outcomes without that
     * source are left out.
     */
    fun acceptance(outcomes: List<ProposalOutcome>, source: ProposalSource): Map<String, Double>
}
