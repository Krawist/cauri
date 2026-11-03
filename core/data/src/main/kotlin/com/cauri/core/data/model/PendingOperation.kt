package com.cauri.core.data.model

import java.time.Instant

/**
 * An operation Cauri read in a message and the user has not confirmed yet (« À confirmer »): one per message, a
 * transfer between two accounts being one operation, its fee riding with it.
 *
 * @property transactionId Transaction read in the message; for a transfer, its outgoing side.
 * @property type Expense, income or transfer.
 * @property amount Amount in minor units of the account's currency; 0 for an operation in another currency.
 * @property foreignAmount Amount in minor units of [foreignCurrency] for an operation in another currency; `null`
 *   otherwise.
 * @property foreignCurrency ISO 4217 code of [foreignAmount]; `null` when the operation was in the account's currency.
 * @property accountName Account the money left, or arrived on for an income.
 * @property toAccountName Account the money arrived on for a transfer; `null` otherwise.
 * @property counterparty Merchant or person on the other side, as the message names them; `null` when unknown.
 * @property occurredAt When the money moved.
 */
data class PendingOperation(
    val transactionId: Long,
    val type: PendingType,
    val amount: Long,
    val foreignAmount: Long?,
    val foreignCurrency: String?,
    val accountName: String,
    val toAccountName: String?,
    val counterparty: String?,
    val occurredAt: Instant,
)
