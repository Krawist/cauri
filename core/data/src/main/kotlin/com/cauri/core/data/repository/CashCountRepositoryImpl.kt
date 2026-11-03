package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.CashCountDao
import com.cauri.core.data.database.dao.TransactionDao
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.CashBalance
import com.cauri.core.data.service.CashBalanceService
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Default implementation for [CashCountRepository]. */
@Singleton
internal class CashCountRepositoryImpl @Inject constructor(
    private val cashCountDao: CashCountDao,
    private val transactionDao: TransactionDao,
    private val cashBalanceService: CashBalanceService,
) : CashCountRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val balance: Flow<CashBalance?> = cashCountDao.observeLatest().flatMapLatest { count ->
        if (count == null) {
            flowOf(null)
        } else {
            transactionDao
                .observeOnAccountAfter(
                    accountId = CASH_ACCOUNT_ID,
                    after = count.countedAt,
                    excluded = TransactionStatus.NOT_COUNTED,
                )
                .map { transactions -> cashBalanceService.estimate(count = count, transactions = transactions) }
        }
    }

    override suspend fun addCount(amount: Long, countedAt: Instant, expectedAmount: Long?) {
        cashCountDao.insert(CashCountEntity(countedAt = countedAt, amount = amount, expectedAmount = expectedAmount))
        if (expectedAmount != null && amount > expectedAmount) {
            transactionDao.insert(
                TransactionEntity(
                    accountId = CASH_ACCOUNT_ID,
                    type = TransactionType.INCOME,
                    status = TransactionStatus.CONFIRMED,
                    origin = TransactionOrigin.RECONCILIATION,
                    amount = amount - expectedAmount,
                    occurredAt = countedAt,
                    categoryId = null,
                    placeId = null,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = null,
                    note = null,
                    foreignAmount = null,
                    foreignCurrency = null,
                ),
            )
        }
    }
}
