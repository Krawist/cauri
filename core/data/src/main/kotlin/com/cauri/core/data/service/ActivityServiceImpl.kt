package com.cauri.core.data.service

import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.PendingType
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [ActivityService]. */
@Singleton
internal class ActivityServiceImpl @Inject constructor() : ActivityService {
    override fun lines(
        transactions: List<TransactionEntity>,
        counts: List<CashCountEntity>,
        accounts: Map<Long, AccountEntity>,
        categories: Map<Long, String>,
    ): List<ActivityLine> {
        // The card of a visit is an operation once the user says what was spent: waiting or set aside, it is none.
        val shown = transactions.filter {
            it.status != TransactionStatus.MERGED &&
                (it.origin != TransactionOrigin.VISIT || it.status == TransactionStatus.CONFIRMED)
        }
        // A fee rides with its operation, on its account and at its time: the line shows the whole amount debited.
        val (fees, moves) = shown.partition { it.isFee() }
        val feesOf = fees.groupByTo(mutableMapOf()) { it.accountId to it.occurredAt }
        fun feesFor(vararg sides: TransactionEntity?) = sides.filterNotNull()
            .filter { it.origin == TransactionOrigin.SIGNAL }
            .flatMap { feesOf.remove(it.accountId to it.occurredAt).orEmpty() }
        val (transfers, others) = moves.partition { it.transferGroup != null }
        val (split, single) = others.partition { it.splitGroup != null }
        val transferLines = transfers.groupBy { it.transferGroup }.values.mapNotNull { sides ->
            val out = sides.firstOrNull { it.type == TransactionType.TRANSFER_OUT }
            val into = sides.firstOrNull { it.type == TransactionType.TRANSFER_IN }
            out?.toLine(accounts = accounts, categories = categories, to = into?.accountId, fees = feesFor(out, into))
        }
        // The first part keeps the signal and the proposal: the sheet opens it, with the others.
        val splitLines = split.groupBy { it.splitGroup }.values.map { parts ->
            val first = parts.minBy { it.id }
            first.toLine(accounts = accounts, categories = categories, parts = parts, fees = feesFor(first))
        }
        val singleLines = single.map {
            it.toLine(
                accounts = accounts,
                categories = categories,
                fees = if (it.type == TransactionType.EXPENSE) feesFor(it) else emptyList(),
            )
        }
        // A fee whose operation was not found keeps a line of its own.
        val feeLines = feesOf.values.flatten().map { it.toLine(accounts = accounts, categories = categories) }
        val operations = transferLines + splitLines + singleLines + feeLines
        val countLines = counts.map { count ->
            ActivityLine.CashCount(
                at = count.countedAt,
                countId = count.id,
                amount = count.amount,
                expectedAmount = count.expectedAmount,
                expenses = shown.count {
                    it.origin == TransactionOrigin.RECONCILIATION &&
                        it.type == TransactionType.EXPENSE &&
                        it.occurredAt == count.countedAt
                },
            )
        }
        // A count comes first among what happened at its time: the expenses of its gap follow it.
        return (countLines + operations).sortedByDescending { it.at }
    }
}

// The fee of an operation read in a message, or kept by an operator on a transfer: never a signal of its own.
private fun TransactionEntity.isFee() = type == TransactionType.EXPENSE &&
    origin == TransactionOrigin.SIGNAL &&
    rawSignalId == null &&
    splitGroup == null

// The line of [this], with the account the money arrived on for a transfer, the [parts] of a split operation and the
// [fees] of its message.
private fun TransactionEntity.toLine(
    accounts: Map<Long, AccountEntity>,
    categories: Map<Long, String>,
    to: Long? = null,
    parts: List<TransactionEntity> = listOf(this),
    fees: List<TransactionEntity> = emptyList(),
): ActivityLine.Operation {
    // A fee never waits for a confirmation of its own.
    val fee = isFee()
    return ActivityLine.Operation(
        at = occurredAt,
        transactionId = id,
        type = when (type) {
            TransactionType.EXPENSE -> PendingType.EXPENSE
            TransactionType.INCOME -> PendingType.INCOME
            TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN -> PendingType.TRANSFER
        },
        status = when (status) {
            TransactionStatus.DETECTED -> if (fee) ActivityStatus.CONFIRMED else ActivityStatus.PENDING
            TransactionStatus.CONFIRMED -> ActivityStatus.CONFIRMED
            TransactionStatus.DISMISSED, TransactionStatus.MERGED -> ActivityStatus.SET_ASIDE
        },
        amount = parts.sumOf { it.amount } + fees.sumOf { it.amount },
        accountName = accounts.getValue(accountId).name,
        accountKind = accounts.getValue(accountId).kind,
        toAccountName = to?.let { accounts.getValue(it).name },
        toAccountKind = to?.let { accounts.getValue(it).kind },
        categoryName = categoryId?.let(categories::get),
        parts = parts.size,
        counterparty = counterparty,
        note = note,
        fees = fees.sumOf { it.amount },
        fee = fee,
        reconciled = origin == TransactionOrigin.RECONCILIATION,
    )
}
