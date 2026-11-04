package com.cauri.core.data.repository

import com.cauri.core.data.model.IncomeSplit
import com.cauri.core.data.model.IncomeSplitProposal
import com.cauri.core.data.model.PendingIncome
import kotlinx.coroutines.flow.Flow

/** The incomes arrived that Cauri offers to split, savings first, and how the user split them. */
interface IncomeSplitRepository {
    /**
     * The newest income of the user's threshold or more arrived within a week and never answered, again after each
     * change; `null` when there is none.
     */
    val pending: Flow<PendingIncome?>

    /** The split Cauri proposes for [income] with [savingsRate] percent put aside. */
    suspend fun proposal(income: PendingIncome, savingsRate: Int): IncomeSplitProposal

    /** Keeps how the user split the income [transactionId]. */
    suspend fun split(transactionId: Long, split: IncomeSplit)

    /** Keeps that the user chose not to split the income [transactionId]: Cauri offers it no more. */
    suspend fun dismiss(transactionId: Long)
}
