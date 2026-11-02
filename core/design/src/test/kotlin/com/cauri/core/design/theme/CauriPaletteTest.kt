package com.cauri.core.design.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CauriPaletteTest {
    @Nested
    inner class Contrast {
        @ParameterizedTest(name = "{2} {0}")
        @MethodSource("com.cauri.core.design.theme.CauriPaletteTest#textColors")
        fun `should reach 4·5 to 1 for text on the background`(token: String, palette: CauriPalette) {
            // given
            val text = palette.textColor(token)

            // when
            val ratio = contrastRatio(text, palette.background)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{2} {0}")
        @MethodSource("com.cauri.core.design.theme.CauriPaletteTest#textColors")
        fun `should reach 4·5 to 1 for text on a glass surface`(token: String, palette: CauriPalette) {
            // given
            val text = palette.textColor(token)
            val glass = palette.glass.compositeOver(palette.background)

            // when
            val ratio = contrastRatio(text, glass)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("com.cauri.core.design.theme.CauriPaletteTest#palettes")
        fun `should reach 4·5 to 1 for text on the accent`(palette: CauriPalette) {
            // when
            val ratio = contrastRatio(palette.onAccent, palette.accent)

            // then
            ratio shouldBeGreaterThanOrEqual MIN_TEXT_CONTRAST
        }
    }

    // WCAG 2 contrast ratio between two opaque colours.
    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun CauriPalette.textColor(token: String): Color = when (token) {
        "ink" -> ink
        "muted" -> muted
        "accentText" -> accentText
        "goldText" -> goldText
        "coralText" -> coralText
        "greenText" -> greenText
        else -> error("Unknown text token $token")
    }

    companion object {
        private const val MIN_TEXT_CONTRAST = 4.5

        private val textTokens = listOf("ink", "muted", "accentText", "goldText", "coralText", "greenText")

        // The mode is the last argument: JUnit only uses it in the test name.
        @JvmStatic
        fun palettes(): List<Arguments> = listOf(
            Arguments.of(LightPalette, "light"),
            Arguments.of(DarkPalette, "dark"),
        )

        @JvmStatic
        fun textColors(): List<Arguments> = palettes().flatMap { arguments ->
            val (palette, mode) = arguments.get()
            textTokens.map { token -> Arguments.of(token, palette, mode) }
        }
    }
}
