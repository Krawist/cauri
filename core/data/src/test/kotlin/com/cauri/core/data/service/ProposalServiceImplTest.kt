package com.cauri.core.data.service

import com.cauri.core.data.model.ProposalSource
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ProposalServiceImplTest {
    private val service = ProposalServiceImpl()

    @Nested
    inner class Acceptance {
        @Test
        fun `should give the share of proposals confirmed unchanged per merchant`() {
            // given
            val outcomes = listOf(
                outcome(counterparty = "BOULANGERIE", proposedAmount = 2_500, amount = 2_500),
                outcome(counterparty = "BOULANGERIE", proposedAmount = 2_500, amount = 3_000),
                outcome(counterparty = "PHARMACIE", proposedAmount = 3_200, amount = 3_200),
            )

            // when
            val acceptance = service.acceptance(outcomes = outcomes, source = ProposalSource.MERCHANT)

            // then
            acceptance shouldBe mapOf("BOULANGERIE" to 0.5, "PHARMACIE" to 1.0)
        }

        @Test
        fun `should count a changed category as a proposal not kept`() {
            // given
            val outcomes = listOf(outcome(templateVersion = "om.payment@1", proposedCategoryId = 1, categoryId = 2))

            // when
            val acceptance = service.acceptance(outcomes = outcomes, source = ProposalSource.TEMPLATE)

            // then
            acceptance shouldBe mapOf("om.payment@1" to 0.0)
        }

        @Test
        fun `should leave out the outcomes without the source`() {
            // given
            val outcomes = listOf(outcome(proposedAmount = 500, amount = 500))

            // when
            val acceptance = service.acceptance(outcomes = outcomes, source = ProposalSource.PLACE)

            // then
            acceptance shouldBe emptyMap()
        }
    }

    private fun outcome(
        counterparty: String? = null,
        templateVersion: String? = null,
        proposedCategoryId: Long? = null,
        categoryId: Long? = null,
        proposedAmount: Long? = null,
        amount: Long = 0,
    ) = ProposalOutcome(
        placeId = null,
        counterparty = counterparty,
        templateVersion = templateVersion,
        proposedCategoryId = proposedCategoryId,
        categoryId = categoryId,
        proposedAmount = proposedAmount,
        amount = amount,
    )
}
