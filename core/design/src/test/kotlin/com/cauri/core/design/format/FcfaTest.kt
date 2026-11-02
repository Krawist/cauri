package com.cauri.core.design.format

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FcfaTest {
    @Nested
    inner class FormatFcfa {
        @ParameterizedTest
        @CsvSource("0, 0", "500, 500", "2500, 2 500", "1200000, 1 200 000")
        fun `should group thousands with a thin space`(amount: Long, expected: String) {
            // when
            val formatted = formatFcfa(amount)

            // then
            formatted shouldBe expected.replace(' ', THIN_SPACE)
        }

        @Test
        fun `should keep the minus sign of a negative amount`() {
            // when
            val formatted = formatFcfa(-7500)

            // then
            formatted shouldBe "-7${THIN_SPACE}500"
        }
    }
}
