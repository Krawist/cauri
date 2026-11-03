package com.cauri.core.data.service

import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.MonthSpending
import java.time.Instant

/** Builds « Activité » out of the transactions and the cash counts. */
internal interface ActivityService {
    /**
     * The lines of « Activité », newest first: one per operation, a transfer and a split operation each being one, its
     * fees in its amount, and one per cash count, above the expenses its gap was split into. Merged entries are left
     * out; [accounts] and [categories] (names) by id.
     */
    fun lines(
        transactions: List<TransactionEntity>,
        counts: List<CashCountEntity>,
        accounts: Map<Long, AccountEntity>,
        categories: Map<Long, String>,
    ): List<ActivityLine>

    /**
     * What [lines] spent from [from] on, cash apart from digital: the expenses and the fees of the transfers, the money
     * set aside left out.
     */
    fun spending(lines: List<ActivityLine>, from: Instant): MonthSpending
}
