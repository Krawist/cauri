package com.cauri.core.parsing

import kotlinx.serialization.Serializable

/**
 * A real message of an operator or a bank, anonymised (names, numbers, references, amounts and dates replaced), from
 * `collected_messages.json`.
 *
 * @property sender SMS or mail sender name, as the notification titles it.
 * @property packageName App that posted it: a mail app for a mail; `null` for the SMS app.
 * @property text Text of the message, line breaks and spaces kept.
 * @property expected What the templates must read in it; `null` when it reports no operation and must stay
 *   unrecognised.
 */
@Serializable
internal data class CollectedMessage(
    val sender: String,
    val text: String,
    val expected: ExpectedReading?,
    val packageName: String? = null,
)
