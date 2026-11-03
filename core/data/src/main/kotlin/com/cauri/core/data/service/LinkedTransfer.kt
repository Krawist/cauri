package com.cauri.core.data.service

import com.cauri.core.data.database.entity.TransactionEntity

/**
 * A debit and a credit turned into the two sides of one transfer.
 *
 * @property debit Outgoing side, for the amount that arrived.
 * @property credit Incoming side.
 * @property fee Expense on the debited account for what left but did not arrive; `null` when both amounts were equal.
 */
internal data class LinkedTransfer(
    val debit: TransactionEntity,
    val credit: TransactionEntity,
    val fee: TransactionEntity?,
)
