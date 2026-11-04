package com.cauri.core.parsing

import com.cauri.core.parsing.model.Direction
import com.cauri.core.parsing.model.MessageKind
import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog
import java.util.Currency
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Default implementation for [ModelMessageReader]. */
@Singleton
class ModelMessageReaderImpl @Inject constructor() : ModelMessageReader {
    private val json = Json { ignoreUnknownKeys = true }

    override fun prompt(message: ReceivedMessage): String = """
        This SMS or notification comes from a mobile money operator or a bank in Cameroon. Say whether it reports an
        operation on the user's account. Answer with one JSON object only: {"kind": "PAYMENT" (to a merchant or a
        bill), "TRANSFER" (money sent or received), "WITHDRAWAL" (cash taken out), "DEPOSIT" (cash put in) or "NONE"
        (promotion, code, failure, reminder), "direction": "DEBIT" (money leaves the account) or "CREDIT" (it
        arrives), "amount", "fees" and "balance" exactly as written in the message or null, "counterparty": the
        merchant or person named, or null}.
        Never compute, add or convert an amount.
        Message: ${message.text}
    """.trimIndent()

    override fun read(answer: String, message: ReceivedMessage, catalog: TemplateCatalog): ParsedMessage? {
        // The account is the one of the sender's templates; a sender of several providers is no single account.
        val template = catalog.templates.filter { it.isSentBy(message) }
            .takeIf { templates -> templates.map { it.provider }.distinct().size == 1 }
            ?.first()
        val reply = reply(answer)
        val kind = reply?.kind?.let { value -> MessageKind.entries.firstOrNull { it.name == value } }
        val direction = reply?.direction?.let { value -> Direction.entries.firstOrNull { it.name == value } }
        if (template == null || kind == null || direction == null) return null
        val digits = Currency.getInstance(template.currency).defaultFractionDigits
        val written = writtenAmounts(message.text, digits)
        val amountOf = { value: String? -> value?.let { readAmount(it, DECIMAL, digits) } }
        val amount = amountOf(reply.amount)
        val fees = amountOf(reply.fees)
        val balance = amountOf(reply.balance)
        // Every value the model gave is one of the message: a value it computed, or could not copy, leaves the message
        // unread.
        val values = listOf(reply.amount to amount, reply.fees to fees, reply.balance to balance)
        val copied = amount != null &&
            amount > 0 &&
            values.all { (given, value) -> given == null || (value != null && value in written) }
        return ParsedMessage(
            templateId = ModelMessageReader.TEMPLATE_ID,
            templateVersion = ModelMessageReader.VERSION,
            provider = template.provider,
            kind = kind,
            direction = direction,
            amount = amount ?: 0,
            currency = template.currency,
            fees = fees,
            balance = balance,
            counterparty = reply.counterparty?.trim()?.takeIf { it.isNotEmpty() },
            reference = null,
            occurredAt = message.receivedAt,
        ).takeIf { copied }
    }

    // The object in [answer], whatever the model wrote around it; `null` when there is none.
    private fun reply(answer: String): Reply? {
        val start = answer.indexOf('{')
        val end = answer.lastIndexOf('}')
        return if (start < 0 || end < start) {
            null
        } else {
            try {
                json.decodeFromString<Reply>(answer.substring(start, end + 1))
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    // Every amount written in [text], in minor units: digits grouped by spaces or not, decimals after a dot or a comma.
    private fun writtenAmounts(text: String, digits: Int): Set<Long> = AMOUNT.findAll(text)
        .mapNotNull { readAmount(it.value.replace(',', DECIMAL), DECIMAL, digits) }
        .toSet()

    // What the model answers; a field it forgot is `null`.
    @Serializable
    private data class Reply(
        val kind: String? = null,
        val direction: String? = null,
        val amount: String? = null,
        val fees: String? = null,
        val balance: String? = null,
        val counterparty: String? = null,
    )

    private companion object {
        val AMOUNT = Regex("\\d{1,3}(?:[ \\u00A0\\u202F]\\d{3})+(?:[.,]\\d+)?|\\d+(?:[.,]\\d+)?")
        const val DECIMAL = '.'
    }
}
