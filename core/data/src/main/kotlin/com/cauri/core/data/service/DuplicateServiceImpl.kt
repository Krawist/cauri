package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [DuplicateService]. */
@Singleton
internal class DuplicateServiceImpl @Inject constructor() : DuplicateService {
    override fun sameOperation(
        transaction: TransactionEntity,
        candidates: List<TransactionEntity>,
    ): TransactionEntity? = candidates
        .filter { candidate ->
            transaction.type in MERGEABLE &&
                candidate.id != transaction.id &&
                candidate.accountId == transaction.accountId &&
                candidate.type == transaction.type &&
                candidate.amount == transaction.amount &&
                gap(candidate, transaction) <= DuplicateService.WINDOW &&
                (detected(candidate) && manual(transaction) || manual(candidate) && detected(transaction))
        }
        .minByOrNull { gap(it, transaction) }

    override fun merged(detected: TransactionEntity, manual: TransactionEntity): TransactionEntity = detected.copy(
        status = TransactionStatus.CONFIRMED,
        categoryId = manual.categoryId ?: detected.categoryId,
        note = manual.note ?: detected.note,
        placeId = manual.placeId ?: detected.placeId,
    )

    // A fee has no signal of its own: only the operation read in a message can be merged.
    private fun detected(transaction: TransactionEntity) = transaction.origin == TransactionOrigin.SIGNAL &&
        transaction.status == TransactionStatus.DETECTED &&
        transaction.rawSignalId != null

    private fun manual(transaction: TransactionEntity) = transaction.origin == TransactionOrigin.MANUAL &&
        transaction.status == TransactionStatus.CONFIRMED

    private fun gap(first: TransactionEntity, second: TransactionEntity): Duration =
        Duration.between(first.occurredAt, second.occurredAt).abs()

    private companion object {
        val MERGEABLE = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    }
}
