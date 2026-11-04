package com.cauri.core.parsing

import com.cauri.core.parsing.model.MessageTemplate
import com.cauri.core.parsing.model.ParsedMessage
import com.cauri.core.parsing.model.ReceivedMessage
import com.cauri.core.parsing.model.TemplateCatalog
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.util.Currency
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [MessageParser]. Public so that the app's Hilt modules can bind it. */
@Singleton
class MessageParserImpl @Inject constructor() : MessageParser {
    // The patterns of a catalogue are compiled once.
    private val patterns = ConcurrentHashMap<String, Regex>()

    override fun parse(message: ReceivedMessage, catalog: TemplateCatalog, zone: ZoneId): ParsedMessage? {
        val text = message.text.replace(SPACES, " ").trim()
        return catalog.templates
            .asSequence()
            .filter { template -> template.isSentBy(message) }
            .firstNotNullOfOrNull { template ->
                pattern(template).find(text)?.let { match ->
                    read(template = template, match = match, receivedAt = message.receivedAt, zone = zone)
                }
            }
    }

    private fun pattern(template: MessageTemplate): Regex =
        patterns.getOrPut(template.pattern) { Regex(template.pattern, RegexOption.IGNORE_CASE) }

    private fun read(template: MessageTemplate, match: MatchResult, receivedAt: Instant, zone: ZoneId): ParsedMessage? {
        val group = { name: String -> match.value(template = template, name = name) }
        val currency = (group(CURRENCY) ?: template.currency).uppercase()
        val digits = runCatching { Currency.getInstance(currency).defaultFractionDigits }.getOrNull()
        val amountOf = { name: String ->
            digits?.let { group(name)?.let { text -> readAmount(text, template.decimalSeparator, digits) } }
        }
        val amount = amountOf(AMOUNT)
        val fees = amountOf(FEES)
        val balance = amountOf(BALANCE)
        val date = group(DATE)?.let { readDate(text = it, template = template, zone = zone) }
        // A value the pattern found but that cannot be read makes the whole message unrecognised: no number is guessed.
        val readable = listOf(group(FEES) to fees, group(BALANCE) to balance, group(DATE) to date)
            .all { (found, value) -> found == null || value != null }
        return amount?.takeIf { readable }?.let {
            ParsedMessage(
                templateId = template.id,
                templateVersion = template.version,
                provider = template.provider,
                kind = template.kind,
                direction = template.direction,
                amount = it,
                currency = currency,
                fees = fees,
                balance = balance,
                counterparty = group(COUNTERPARTY),
                reference = group(REFERENCE),
                occurredAt = date ?: receivedAt,
            )
        }
    }

    private fun readDate(text: String, template: MessageTemplate, zone: ZoneId): Instant? {
        val formatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern(template.dateFormat)
            .toFormatter(Locale.forLanguageTag(template.dateLocale))
        return try {
            LocalDateTime.parse(text, formatter).atZone(zone).toInstant()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    // A group missing from the pattern is not an error: only the groups the template declares are read.
    private fun MatchResult.value(template: MessageTemplate, name: String): String? =
        if ("(?<$name>" in template.pattern) groups[name]?.value?.trim()?.takeIf { it.isNotEmpty() } else null

    private companion object {
        const val AMOUNT = "amount"
        const val BALANCE = "balance"
        const val COUNTERPARTY = "counterparty"
        const val CURRENCY = "currency"
        const val DATE = "date"
        const val FEES = "fees"
        const val REFERENCE = "reference"

        // Operators write amounts with no-break spaces (« 2 500 F »): every kind of space counts as one.
        val SPACES = Regex("[\\s\\u00A0\\u202F]+")
    }
}
