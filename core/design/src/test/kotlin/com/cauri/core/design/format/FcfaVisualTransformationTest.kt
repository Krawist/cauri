package com.cauri.core.design.format

import androidx.compose.ui.text.AnnotatedString
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class FcfaVisualTransformationTest {
    @Nested
    inner class Filter {
        @ParameterizedTest
        @CsvSource("'', ''", "7, 7", "500, 500", "7500, 7 500", "1200000, 1 200 000")
        fun `should group the digits by thousands with a thin space`(digits: String, expected: String) {
            // when
            val shown = FcfaVisualTransformation.filter(AnnotatedString(digits)).text.text

            // then
            shown shouldBe expected.replace(' ', THIN_SPACE)
        }

        @ParameterizedTest
        @CsvSource("0, 0", "1, 2", "2, 3", "4, 6", "7, 9")
        fun `should keep the cursor just before the same digit in the shown text`(digitOffset: Int, shownOffset: Int) {
            // given
            val mapping = FcfaVisualTransformation.filter(AnnotatedString("1200000")).offsetMapping

            // when
            val mapped = mapping.originalToTransformed(digitOffset)

            // then
            mapped shouldBe shownOffset
        }

        @Test
        fun `should map a position next to a separator back to the digit before it`() {
            // given
            val mapping = FcfaVisualTransformation.filter(AnnotatedString("7500")).offsetMapping

            // when
            val mapped = mapping.transformedToOriginal(2)

            // then
            mapped shouldBe 1
        }
    }
}
