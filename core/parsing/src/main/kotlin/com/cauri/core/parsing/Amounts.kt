package com.cauri.core.parsing

/**
 * Reads an amount written as in a message (« 2 500 », « 1.250.000 », « 2 500,00 ») in minor units, with [fractionDigits]
 * decimals for its currency. Every character other than the digits and [decimalSeparator] is a thousands separator.
 * `null` when it holds no digit, or decimals that the currency cannot hold.
 */
internal fun readAmount(text: String, decimalSeparator: Char?, fractionDigits: Int): Long? {
    val separated = decimalSeparator != null && decimalSeparator in text
    val units = if (separated) text.substringBeforeLast(decimalSeparator) else text
    val decimals = if (separated) text.substringAfterLast(decimalSeparator).filter(Char::isDigit) else ""
    val digits = units.filter(Char::isDigit)
    val lost = decimals.drop(fractionDigits).any { it != '0' }
    return if (digits.isEmpty() || lost) {
        null
    } else {
        (digits + decimals.take(fractionDigits).padEnd(fractionDigits, '0')).toLongOrNull()
    }
}
