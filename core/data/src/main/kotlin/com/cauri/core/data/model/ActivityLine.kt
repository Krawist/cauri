package com.cauri.core.data.model

import java.time.Instant

/** A line of « Activité »: an operation on the user's accounts, or a cash count. */
sealed interface ActivityLine {
    /** When it happened, which orders the timeline. */
    val at: Instant

    /**
     * An operation: one line for a transfer (both sides) and one for an operation split across several categories.
     *
     * @property transactionId Transaction the edit sheet opens: the outgoing side of a transfer, the first part of a
     *   split operation.
     * @property type Expense, income or transfer.
     * @property status Waiting, counted or set aside.
     * @property amount Amount in minor units of the account's currency, the parts of a split operation included; 0 for
     *   an operation in another currency, until the user types what it cost.
     * @property accountName Account the money left, or arrived on for an income.
     * @property accountKind Kind of the account named [accountName].
     * @property toAccountName Account the money arrived on for a transfer; `null` otherwise.
     * @property toAccountKind Kind of the account named [toAccountName]; `null` when not a transfer.
     * @property categoryName Category, of the first part for a split operation; `null` when none.
     * @property parts Number of categories a split operation is spread across; 1 when not split.
     * @property counterparty Merchant or person on the other side, as the message names them; `null` when unknown.
     * @property note Free note of the user, or the name of a habit; `null` when there is none.
     * @property fee Whether it is the fee of an operation read in a message.
     * @property reconciled Whether it comes from the gap of a cash count.
     */
    data class Operation(
        override val at: Instant,
        val transactionId: Long,
        val type: PendingType,
        val status: ActivityStatus,
        val amount: Long,
        val accountName: String,
        val accountKind: AccountKind,
        val toAccountName: String?,
        val toAccountKind: AccountKind?,
        val categoryName: String?,
        val parts: Int,
        val counterparty: String?,
        val note: String?,
        val fee: Boolean,
        val reconciled: Boolean,
    ) : ActivityLine

    /**
     * A count of the cash in pocket.
     *
     * @property countId Identifier of the count.
     * @property amount Amount counted, in minor units of the Cash account's currency.
     * @property expectedAmount Estimated balance at that moment; `null` for the first count.
     * @property expenses Number of expenses the missing money was split into; 0 without gap or with a surplus.
     */
    data class CashCount(
        override val at: Instant,
        val countId: Long,
        val amount: Long,
        val expectedAmount: Long?,
        val expenses: Int,
    ) : ActivityLine
}
