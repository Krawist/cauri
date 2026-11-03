package com.cauri.core.parsing.model

import java.time.Instant

/**
 * What a template read in a message. Amounts are in minor units of [currency], always positive.
 *
 * @property templateId Identifier of the template that read the message.
 * @property templateVersion Version of that template.
 * @property kind What the message reports.
 * @property direction Whether the money leaves the account or comes into it.
 * @property amount Amount of the operation, fees excluded.
 * @property currency ISO 4217 code of the amount, the fees and the balance.
 * @property fees Fees of the operation; `null` when the message gives none.
 * @property balance Balance of the account after the operation; `null` when the message gives none.
 * @property counterparty Merchant or person on the other side, as written; `null` when the message names none.
 * @property reference Reference of the operation given by the operator; `null` when the message gives none.
 * @property occurredAt When the operation happened: the date of the message, or the time of the notification when the
 *   message has no date.
 */
data class ParsedMessage(
    val templateId: String,
    val templateVersion: Int,
    val kind: MessageKind,
    val direction: Direction,
    val amount: Long,
    val currency: String,
    val fees: Long?,
    val balance: Long?,
    val counterparty: String?,
    val reference: String?,
    val occurredAt: Instant,
)
