package com.cauri.core.parsing

import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import kotlinx.serialization.Serializable

/**
 * What the templates must read in a [CollectedMessage]: the values of a parsed message, amounts in minor units.
 *
 * @property templateId Template that must read it.
 * @property kind What the message reports.
 * @property direction Whether the money leaves the account or comes into it.
 * @property amount Amount of the operation.
 * @property currency ISO 4217 code of the amounts.
 * @property fees Fees; `null` when the message gives none.
 * @property balance Balance after the operation; `null` when the message gives none.
 * @property counterparty Merchant or person on the other side; `null` when the message names none.
 * @property reference Reference of the operation; `null` when the message gives none.
 * @property occurredAt When the operation happened, ISO 8601 instant.
 */
@Serializable
internal data class ExpectedReading(
    val templateId: String,
    val kind: MessageKind,
    val direction: Direction,
    val amount: Long,
    val currency: String,
    val fees: Long?,
    val balance: Long?,
    val counterparty: String?,
    val reference: String?,
    val occurredAt: String,
)
