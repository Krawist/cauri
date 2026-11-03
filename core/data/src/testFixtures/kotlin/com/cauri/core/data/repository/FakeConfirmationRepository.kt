package com.cauri.core.data.repository

import com.cauri.core.data.model.PendingOperation

/** In-memory [ConfirmationRepository] for tests: it keeps the answers given. */
class FakeConfirmationRepository : ConfirmationRepository {
    /**
     * An operation confirmed by [confirm].
     *
     * @property operation Operation confirmed.
     * @property categoryId Category chosen; `null` when none.
     * @property proposedCategoryId Category Cauri proposed; `null` when none.
     * @property amount Amount confirmed, in minor units.
     */
    data class Confirmed(
        val operation: PendingOperation,
        val categoryId: Long?,
        val proposedCategoryId: Long?,
        val amount: Long,
    )

    val confirmed = mutableListOf<Confirmed>()
    val dismissed = mutableListOf<PendingOperation>()
    val reopened = mutableListOf<Long>()

    override suspend fun confirm(
        operation: PendingOperation,
        categoryId: Long?,
        proposedCategoryId: Long?,
        amount: Long,
    ) {
        confirmed += Confirmed(
            operation = operation,
            categoryId = categoryId,
            proposedCategoryId = proposedCategoryId,
            amount = amount,
        )
    }

    override suspend fun dismiss(operation: PendingOperation) {
        dismissed += operation
    }

    override suspend fun reopen(transactionId: Long) {
        reopened += transactionId
    }
}
