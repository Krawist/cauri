package com.cauri.core.parsing

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Reads an amount written as in a message (« 2 500 », « 1.250.000 », « 2 500,00 ») in minor units, with
 * [fractionDigits] decimals for its currency. Every character other than the digits and [decimalSeparator] is a
 * thousands separator. Decimals the currency cannot hold are rounded half up: Orange Money writes francs with cents
 * (« 94.07 FCFA »). `null` when it holds no digit or does not fit in a [Long].
 */
internal fun readAmount(text: String, decimalSeparator: Char?, fractionDigits: Int): Long? {
    val separated = decimalSeparator != null && decimalSeparator in text
    val units = (if (separated) text.substringBeforeLast(decimalSeparator) else text).filter(Char::isDigit)
    val decimals = if (separated) text.substringAfterLast(decimalSeparator).filter(Char::isDigit) else ""
    return units.takeIf { it.isNotEmpty() }?.let {
        BigDecimal("$it.${decimals.ifEmpty { "0" }}")
            .setScale(fractionDigits, RoundingMode.HALF_UP)
            .unscaledValue()
            .takeIf { value -> value.bitLength() < Long.SIZE_BITS }
            ?.toLong()
    }
}
