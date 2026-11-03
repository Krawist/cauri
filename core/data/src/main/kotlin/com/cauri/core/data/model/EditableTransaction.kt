package com.cauri.core.data.model

import java.time.Instant

/**
 * A transaction as the edit sheet shows it; for a transfer, its outgoing side with the account the money arrived on.
 *
 * @property transactionId Transaction edited; for a transfer, its outgoing side.
 * @property type Expense, income or transfer.
 * @property amount Whole amount in minor units of the account's currency, [parts] included; 0 for an operation in
 *   another currency, until the user types what it cost.
 * @property foreignAmount Amount in minor units of [foreignCurrency] for an operation in another currency; `null`
 *   otherwise.
 * @property foreignCurrency ISO 4217 code of [foreignAmount]; `null` when the operation was in the account's currency.
 * @property accountId Account the money left, or arrived on for an income.
 * @property toAccountId Account the money arrived on for a transfer; `null` otherwise.
 * @property categoryId Category chosen, of the rest of the amount when split; `null` when none yet (and for a
 *   transfer).
 * @property parts Parts split to other categories before; empty when not split.
 * @property counterparty Merchant or person on the other side, as the message names them; `null` when unknown.
 * @property note Free note of the user; `null` when there is none.
 * @property occurredAt When the money moved.
 * @property read Whether Cauri read it in a message: the message then proves its account, which cannot be changed.
 * @property pending Whether it still waits for the user's confirmation.
 * @property mergeId Merge that joined it with an entry the user typed, to undo with « Séparer »; `null` when none
 *   holds.
 * @property transferLinkId Link that made it a transfer out of two messages, to undo with « Séparer »; `null` when
 *   none holds.
 * @property proposedAmount Amount Cauri proposes while [amount] is still to type, in minor units: the usual one of the
 *   shop visited; `null` for none.
 */
data class EditableTransaction(
    val transactionId: Long,
    val type: PendingType,
    val amount: Long,
    val foreignAmount: Long?,
    val foreignCurrency: String?,
    val accountId: Long,
    val toAccountId: Long?,
    val categoryId: Long?,
    val parts: List<SplitPart>,
    val counterparty: String?,
    val note: String?,
    val occurredAt: Instant,
    val read: Boolean,
    val pending: Boolean,
    val mergeId: Long?,
    val transferLinkId: Long?,
    val proposedAmount: Long? = null,
)
