package com.cauri.core.data.service

import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.CashBalance
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [CashBalanceService]. */
@Singleton
internal class CashBalanceServiceImpl @Inject constructor() : CashBalanceService {
    override fun estimate(count: CashCountEntity, transactions: List<TransactionEntity>): CashBalance {
        val counted = transactions.filter {
            it.status != TransactionStatus.DISMISSED && it.occurredAt > count.countedAt
        }
        val amount = counted.fold(count.amount) { balance, transaction ->
            when (transaction.type) {
                TransactionType.INCOME, TransactionType.TRANSFER_IN -> balance + transaction.amount
                TransactionType.EXPENSE, TransactionType.TRANSFER_OUT -> balance - transaction.amount
            }
        }
        return CashBalance(
            amount = amount,
            countedAt = count.countedAt,
            expensesSinceCount = counted.count { it.type == TransactionType.EXPENSE },
        )
    }
}
