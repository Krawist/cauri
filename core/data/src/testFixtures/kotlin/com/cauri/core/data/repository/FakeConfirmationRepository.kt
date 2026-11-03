package com.cauri.core.data.repository

import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.TransactionEdit

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
    val edited = mutableListOf<TransactionEdit>()

    /** Transactions set aside by [dismiss]. */
    val dismissed = mutableListOf<Long>()
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

    override suspend fun confirm(edit: TransactionEdit) {
        edited += edit
    }

    override suspend fun dismiss(transactionId: Long, type: PendingType) {
        dismissed += transactionId
    }

    override suspend fun reopen(transactionId: Long) {
        reopened += transactionId
    }
}
