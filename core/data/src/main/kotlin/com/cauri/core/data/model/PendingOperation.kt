package com.cauri.core.data.model

import java.time.Instant

/**
 * An operation Cauri read in a message and the user has not confirmed yet (« À confirmer »): one per message, a
 * transfer between two accounts being one operation, its fee riding with it.
 *
 * @property transactionId Transaction read in the message; for a transfer, its outgoing side.
 * @property accountId Account the money left, or arrived on for an income.
 * @property type Expense, income or transfer.
 * @property amount Amount in minor units of the account's currency, without [fees]; 0 for an operation in another
 *   currency.
 * @property fees Fees its message gave, or an operator kept on a transfer, in the same unit: separate expenses, shown
 *   with the operation; 0 for none.
 * @property foreignAmount Amount in minor units of [foreignCurrency] for an operation in another currency; `null`
 *   otherwise.
 * @property foreignCurrency ISO 4217 code of [foreignAmount]; `null` when the operation was in the account's currency.
 * @property accountName Account the money left, or arrived on for an income.
 * @property accountKind Kind of the account named [accountName].
 * @property toAccountName Account the money arrived on for a transfer; `null` otherwise.
 * @property toAccountKind Kind of the account named [toAccountName], [AccountKind.CASH] for a withdrawal; `null` when
 *   not a transfer.
 * @property counterparty Merchant or person on the other side, as the message names them; `null` when unknown.
 * @property occurredAt When the money moved.
 */
data class PendingOperation(
    val transactionId: Long,
    val accountId: Long,
    val type: PendingType,
    val amount: Long,
    val fees: Long,
    val foreignAmount: Long?,
    val foreignCurrency: String?,
    val accountName: String,
    val accountKind: AccountKind,
    val toAccountName: String?,
    val toAccountKind: AccountKind?,
    val counterparty: String?,
    val occurredAt: Instant,
)
