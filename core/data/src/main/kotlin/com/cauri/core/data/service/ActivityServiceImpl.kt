package com.cauri.core.data.service

import com.cauri.core.data.database.entity.AccountEntity
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.ActivityLine
import com.cauri.core.data.model.ActivityStatus
import com.cauri.core.data.model.MonthSpending
import com.cauri.core.data.model.PendingType
import java.time.Instant
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
        val shown = transactions.filter { it.status != TransactionStatus.MERGED }
        val (transfers, others) = shown.partition { it.transferGroup != null }
        val (split, single) = others.partition { it.splitGroup != null }
        val transferLines = transfers.groupBy { it.transferGroup }.values.mapNotNull { sides ->
            val out = sides.firstOrNull { it.type == TransactionType.TRANSFER_OUT }
            val into = sides.firstOrNull { it.type == TransactionType.TRANSFER_IN }
            out?.toLine(accounts = accounts, categories = categories, to = into?.accountId)
        }
        // The first part keeps the signal and the proposal: the sheet opens it, with the others.
        val splitLines = split.groupBy { it.splitGroup }.values.map { parts ->
            parts.minBy { it.id }.toLine(accounts = accounts, categories = categories, parts = parts)
        }
        val operations =
            transferLines + splitLines + single.map { it.toLine(accounts = accounts, categories = categories) }
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

    override fun spending(lines: List<ActivityLine>, from: Instant): MonthSpending {
        val spent = lines.filterIsInstance<ActivityLine.Operation>().filter {
            it.type == PendingType.EXPENSE && it.status != ActivityStatus.SET_ASIDE && !it.at.isBefore(from)
        }
        val (cash, digital) = spent.partition { it.accountKind == AccountKind.CASH }
        return MonthSpending(cash = cash.sumOf { it.amount }, digital = digital.sumOf { it.amount })
    }
}

// The line of [this], with the account the money arrived on for a transfer, and the [parts] of a split operation.
private fun TransactionEntity.toLine(
    accounts: Map<Long, AccountEntity>,
    categories: Map<Long, String>,
    to: Long? = null,
    parts: List<TransactionEntity> = listOf(this),
): ActivityLine.Operation {
    // A fee rides with the operation read in its message: it never waits for a confirmation of its own.
    val fee = type == TransactionType.EXPENSE &&
        origin == TransactionOrigin.SIGNAL &&
        rawSignalId == null &&
        splitGroup == null
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
        amount = parts.sumOf { it.amount },
        accountName = accounts.getValue(accountId).name,
        accountKind = accounts.getValue(accountId).kind,
        toAccountName = to?.let { accounts.getValue(it).name },
        toAccountKind = to?.let { accounts.getValue(it).kind },
        categoryName = categoryId?.let(categories::get),
        parts = parts.size,
        counterparty = counterparty,
        note = note,
        fee = fee,
        reconciled = origin == TransactionOrigin.RECONCILIATION,
    )
}
