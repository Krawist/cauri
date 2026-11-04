package com.cauri.core.parsing.model

import kotlinx.serialization.Serializable

/**
 * One message format of one sender. [pattern] is a regular expression, matched case-insensitively against the
 * message with its spaces collapsed, whose named groups give the values: `amount` (required), and optionally `fees`,
 * `balance`, `counterparty`, `reference`, `date` and `currency`.
 *
 * @property id Stable identifier, `<sender>.<message>` (« orange-money.payment »).
 * @property version Version of this template, raised at each change of its pattern or values.
 * @property provider Code of the operator, bank or card whose account the message is about (« ORANGE_MONEY »), as
 *   stored on the user's accounts.
 * @property senders Who sends it: SMS or mail sender names (« OrangeMoney »), case and spaces ignored, or package
 *   names of apps.
 * @property kind What the message reports.
 * @property direction Whether the money leaves the account or comes into it.
 * @property currency ISO 4217 code of the amounts (« XAF »), unless the `currency` group gives it, as for card payments
 *   abroad (« 9.99 USD »).
 * @property pattern Regular expression with the named groups.
 * @property decimalSeparator Separator of the decimals in the amounts; `null` when they are written without decimals.
 *   Every other character than digits is taken as a thousands separator.
 * @property dateFormat `java.time` pattern of the `date` group, with the time (« dd/MM/yyyy HH:mm »); `null` when the
 *   message has no date, and the time of the notification is used.
 * @property dateLocale Language of the month and day names in the date, as a BCP 47 tag.
 * @property ownAccount Whether the other side is an account of the user's own, named in `counterparty`: a transfer
 *   between their accounts (« vers votre compte UBA »), never a person.
 */
@Serializable
data class MessageTemplate(
    val id: String,
    val version: Int,
    val provider: String,
    val senders: List<String>,
    val kind: MessageKind,
    val direction: Direction,
    val currency: String,
    val pattern: String,
    val decimalSeparator: Char? = null,
    val dateFormat: String? = null,
    val dateLocale: String = "fr",
    val ownAccount: Boolean = false,
)
