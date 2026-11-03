package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.ConfirmationDao
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.model.PendingOperation
import com.cauri.core.data.model.PendingType
import java.time.Clock
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

    override suspend fun dismiss(operation: PendingOperation) = confirmationDao.dismiss(
        id = operation.transactionId,
        outcome = if (operation.type ==
            PendingType.EXPENSE
        ) {
            DecisionOutcome.NOTHING_SPENT
        } else {
            DecisionOutcome.DISMISSED
        },
        at = clock.instant(),
    )

    override suspend fun reopen(transactionId: Long) = confirmationDao.reopen(transactionId)
}
