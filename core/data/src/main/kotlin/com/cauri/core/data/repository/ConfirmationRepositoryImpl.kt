package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.ConfirmationDao
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import com.cauri.core.data.model.TransactionEdit
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ConfirmationRepository]. */
@Singleton
internal class ConfirmationRepositoryImpl @Inject constructor(
    private val confirmationDao: ConfirmationDao,
    private val clock: Clock,
) : ConfirmationRepository {
    override suspend fun confirm(
        operation: PendingOperation,
        categoryId: Long?,
        proposedCategoryId: Long?,
        amount: Long,
    ) = confirmationDao.confirm(
        id = operation.transactionId,
        categoryId = categoryId,
        proposedCategoryId = proposedCategoryId,
        amount = amount,
        outcome = DecisionOutcome.CONFIRMED,
        at = clock.instant(),
    )

    override suspend fun confirm(edit: TransactionEdit) {
        val transaction = confirmationDao.getTransaction(edit.transactionId) ?: return
        val transfer = transaction.transferGroup != null
        // A transfer has no category, hence nothing to split.
        val parts = if (transfer) emptyList() else edit.parts
        val edited = transaction.copy(
            // The message proves the account, and a transfer keeps both of its accounts.
            accountId = if (transaction.rawSignalId != null || transfer) transaction.accountId else edit.accountId,
            status = TransactionStatus.CONFIRMED,
            amount = edit.amount - parts.sumOf { it.amount },
            occurredAt = edit.occurredAt,
            categoryId = edit.categoryId.takeUnless { transfer },
            note = edit.note?.trim()?.takeIf { it.isNotEmpty() },
            proposedCategoryId = edit.proposedCategoryId,
            // Split again, it keeps its group; the parts typed replace those it had.
            splitGroup = if (parts.isEmpty()) null else transaction.splitGroup ?: UUID.randomUUID().toString(),
        )
        confirmationDao.confirmEdited(
            transaction = edited,
            formerSplitGroup = transaction.splitGroup,
            // Each part rides with the operation, without its signal nor Cauri's proposal, kept on the edited one.
            parts = parts.map {
                edited.copy(
                    id = 0,
                    amount = it.amount,
                    categoryId = it.categoryId,
                    rawSignalId = null,
                    foreignAmount = null,
                    foreignCurrency = null,
                    proposedCategoryId = null,
                    proposedAmount = null,
                )
            },
            outcome = DecisionOutcome.CONFIRMED,
            at = clock.instant(),
        )
    }

    override suspend fun dismiss(transactionId: Long, type: PendingType) = confirmationDao.dismiss(
        id = transactionId,
        outcome = if (type == PendingType.EXPENSE) DecisionOutcome.NOTHING_SPENT else DecisionOutcome.DISMISSED,
        at = clock.instant(),
    )

    override suspend fun reopen(transactionId: Long) = confirmationDao.reopen(transactionId)
}
