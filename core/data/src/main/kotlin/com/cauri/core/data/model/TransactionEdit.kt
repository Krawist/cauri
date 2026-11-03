package com.cauri.core.data.model

import java.time.Instant

/**
 * What the user kept in the edit sheet. The [parts] split the amount: each becomes a transaction of its own, linked to
 * the edited one, which keeps the rest in [categoryId].
 *
 * @property transactionId Transaction edited; for a transfer, its outgoing side.
 * @property amount Whole amount, parts included, in minor units of the account's currency.
 * @property categoryId Category of the rest of the amount; `null` for none (and for a transfer).
 * @property proposedCategoryId Category Cauri proposed, kept next to [categoryId]; `null` when it proposed none.
 * @property accountId Account of the transaction; ignored for one read in a message or a transfer.
 * @property occurredAt When the money moved.
 * @property note Free note of the user; `null` for none.
 * @property parts Parts split to other categories; empty when not split.
 */
data class TransactionEdit(
    val transactionId: Long,
    val amount: Long,
    val categoryId: Long?,
    val proposedCategoryId: Long?,
    val accountId: Long,
    val occurredAt: Instant,
    val note: String?,
    val parts: List<SplitPart>,
)
