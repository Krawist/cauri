package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.Withdrawal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [TransactionRepository]. */
@Singleton
internal class TransactionRepositoryImpl @Inject constructor(private val transactionDao: TransactionDao) :
    TransactionRepository {
    override suspend fun recordWithdrawal(withdrawal: Withdrawal) {
        val parsed = withdrawal.rawSignalId != null
        val side = TransactionEntity(
            accountId = withdrawal.accountId,
            type = TransactionType.TRANSFER_OUT,
            status = if (parsed) TransactionStatus.DETECTED else TransactionStatus.CONFIRMED,
            origin = if (parsed) TransactionOrigin.SIGNAL else TransactionOrigin.MANUAL,
            amount = withdrawal.amount,
            occurredAt = withdrawal.occurredAt,
            categoryId = null,
            placeId = null,
            rawSignalId = null,
            transferGroup = UUID.randomUUID().toString(),
            counterparty = null,
            note = null,
        )
        // The signal is kept on the side it comes from: a signal creates at most one transaction.
        val out = side.copy(rawSignalId = withdrawal.rawSignalId)
        val into = side.copy(accountId = CASH_ACCOUNT_ID, type = TransactionType.TRANSFER_IN)
        val fee = withdrawal.fee?.let { side.copy(type = TransactionType.EXPENSE, amount = it, transferGroup = null) }
        transactionDao.insertAll(listOfNotNull(out, into, fee))
    }
}
