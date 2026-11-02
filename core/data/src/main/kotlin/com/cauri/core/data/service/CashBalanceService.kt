package com.cauri.core.data.service

import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.model.CashBalance

/** Rules of the estimated Cash balance. */
internal interface CashBalanceService {
    /**
     * Estimates the cash in pocket from the last [count] and the Cash account's [transactions] since it: withdrawals
     * and income add up, expenses and deposits take away. Dismissed transactions are not counted.
     */
    fun estimate(count: CashCountEntity, transactions: List<TransactionEntity>): CashBalance
}
