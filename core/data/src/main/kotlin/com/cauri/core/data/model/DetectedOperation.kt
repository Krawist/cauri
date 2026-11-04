package com.cauri.core.data.model

import java.time.Instant

/**
 * An operation read in a raw signal, to record as transactions waiting for the user's confirmation.
 *
 * @property rawSignalId Signal it was read in.
 * @property templateVersion Template that read it, as `<template id>@<version>`.
 * @property accountId Account the operation happened on.
 * @property type What the message reports.
 * @property amount Amount in minor units of the account's currency, fees excluded; 0 when the operation was in another
 *   currency (see [foreignAmount]).
 * @property fee Fee in minor units of the account's currency; `null` when there is none.
 * @property foreignAmount Amount in minor units of [foreignCurrency] when the operation was in another currency;
 *   `null` otherwise.
 * @property foreignCurrency ISO 4217 code of [foreignAmount]; `null` when the operation was in the account's currency.
 * @property balance Balance of the account after the operation, in minor units of its currency; `null` when the
 *   message gives none, or gives it in another currency.
 * @property counterparty Merchant or person on the other side, as the message names them; `null` when unknown.
 * @property occurredAt When the money moved.
 * @property counterpartAccountId The user's other account of a [DetectedType.TRANSFER_OUT] or a
 *   [DetectedType.TRANSFER_IN]; `null` for the other types, the Cash account being the other side of a withdrawal or a
 *   deposit.
 */
data class DetectedOperation(
    val rawSignalId: Long,
    val templateVersion: String,
    val accountId: Long,
    val type: DetectedType,
    val amount: Long,
    val fee: Long?,
    val foreignAmount: Long?,
    val foreignCurrency: String?,
    val balance: Long?,
    val counterparty: String?,
    val occurredAt: Instant,
    val counterpartAccountId: Long? = null,
)
