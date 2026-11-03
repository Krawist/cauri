package com.cauri.core.parsing

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class AmountsTest {
    @Nested
    inner class ReadAmount {
        @ParameterizedTest
        @CsvSource(
            "'2500', 2500",
            "'2 500', 2500",
            "'1.250.000', 1250000",
            "'1,250,000', 1250000",
            "'2 500 FCFA', 2500",
        )
        fun `should read an amount without decimals whatever its thousands separators`(text: String, expected: Long) {
            // when
            val amount = readAmount(text = text, decimalSeparator = null, fractionDigits = 0)

            // then
            amount shouldBe expected
        }

        @ParameterizedTest
        @CsvSource("'2 500,00', ',', 2500", "'2,500.00', '.', 2500", "'1.250.000,00', ',', 1250000")
        fun `should drop zero decimals of a currency without decimals`(text: String, separator: Char, expected: Long) {
            // when
            val amount = readAmount(text = text, decimalSeparator = separator, fractionDigits = 0)

            // then
            amount shouldBe expected
        }

        @ParameterizedTest
        @CsvSource("'12,5', 1250", "'12,50', 1250", "'12', 1200")
        fun `should read the decimals of a currency that has some`(text: String, expected: Long) {
            // when
            val amount = readAmount(text = text, decimalSeparator = ',', fractionDigits = 2)

            // then
            amount shouldBe expected
        }

        @Test
        fun `should not read decimals that the currency cannot hold`() {
            // when
            val amount = readAmount(text = "2 500,50", decimalSeparator = ',', fractionDigits = 0)

            // then
            amount.shouldBeNull()
        }

        @Test
        fun `should not read an amount without digits`() {
            // when
            val amount = readAmount(text = "FCFA", decimalSeparator = null, fractionDigits = 0)

            // then
            amount.shouldBeNull()
        }
    }
}
