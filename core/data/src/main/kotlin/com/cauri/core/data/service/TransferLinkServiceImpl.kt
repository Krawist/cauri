package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionType
import java.time.Duration
import javax.inject.Inject
import kotlin.math.absoluteValue
import javax.inject.Singleton

/** Default implementation for [TransferLinkService]. */
@Singleton
internal class TransferLinkServiceImpl @Inject constructor() : TransferLinkService {
    override fun pairs(transactions: List<TransactionEntity>): List<Pair<TransactionEntity, TransactionEntity>> {
        val debits = transactions.filter { it.type == TransactionType.EXPENSE }.toMutableList()
        return transactions
            .filter { it.type == TransactionType.INCOME }
            .sortedBy { it.occurredAt }
            .mapNotNull { credit ->
                debits
                    .filter { debit -> sameTransfer(debit = debit, credit = credit) }
                    .minWithOrNull(compareBy({ it.amount - credit.amount }, { gap(it, credit) }))
                    ?.also { debits -= it }
                    ?.let { it to credit }
            }
    }

    override fun waitingSide(message: TransactionEntity, sides: List<TransactionEntity>): TransactionEntity? = sides
        .filter { side ->
            side.rawSignalId == null &&
                side.transferGroup != null &&
                side.accountId == message.accountId &&
                side.type == message.type &&
                gap(side, message) <= TransferLinkService.WINDOW &&
                when (message.type) {
                    // The side waiting holds the amount that left: what arrives is it, or a little less.
                    TransactionType.TRANSFER_IN -> fits(debit = side.amount, credit = message.amount)
                    else -> fits(debit = message.amount, credit = side.amount)
                }
        }
        .minWithOrNull(compareBy({ (it.amount - message.amount).absoluteValue }, { gap(it, message) }))

    override fun linked(debit: TransactionEntity, credit: TransactionEntity, group: String): LinkedTransfer {
        val kept = debit.amount - credit.amount
        return LinkedTransfer(
            debit = debit.copy(type = TransactionType.TRANSFER_OUT, amount = credit.amount, transferGroup = group),
            credit = credit.copy(type = TransactionType.TRANSFER_IN, transferGroup = group),
            fee = kept.takeIf { it > 0 }?.let {
                debit.copy(
                    id = 0,
                    type = TransactionType.EXPENSE,
                    amount = it,
                    categoryId = null,
                    placeId = null,
                    rawSignalId = null,
                    transferGroup = null,
                    counterparty = null,
                    note = null,
                )
            },
        )
    }

    private fun sameTransfer(debit: TransactionEntity, credit: TransactionEntity) =
        debit.accountId != credit.accountId &&
            gap(debit, credit) <= TransferLinkService.WINDOW &&
            fits(debit = debit.amount, credit = credit.amount)

    // What arrived is what left, or less by at most 3 %: the operator kept a fee.
    private fun fits(debit: Long, credit: Long) =
        credit <= debit && (debit - credit) * PERCENT <= debit * MAX_FEE_PERCENT

    private fun gap(first: TransactionEntity, second: TransactionEntity): Duration =
        Duration.between(first.occurredAt, second.occurredAt).abs()

    private companion object {
        const val PERCENT = 100
        const val MAX_FEE_PERCENT = 3
    }
}
