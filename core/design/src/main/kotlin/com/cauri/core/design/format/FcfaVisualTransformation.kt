package com.cauri.core.design.format

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Shows the digits of an amount field grouped by thousands with a thin space, as [formatFcfa] does, while the field
 * keeps the bare digits: "7500" is shown as "7 500". The field must only accept digits.
 */
object FcfaVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val shown = StringBuilder()
        // Position in the shown text of each position in the digits, and the other way round.
        val digitToShown = IntArray(digits.length + 1)
        digits.forEachIndexed { index, digit ->
            if (index > 0 && (digits.length - index) % GROUP_SIZE == 0) shown.append(THIN_SPACE)
            digitToShown[index] = shown.length
            shown.append(digit)
        }
        digitToShown[digits.length] = shown.length
        val shownToDigit = IntArray(shown.length + 1)
        var digitsBefore = 0
        for (position in 0..shown.length) {
            shownToDigit[position] = digitsBefore
            if (position < shown.length && shown[position] != THIN_SPACE) digitsBefore++
        }
        return TransformedText(
            text = AnnotatedString(shown.toString()),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = digitToShown[offset]

                override fun transformedToOriginal(offset: Int) = shownToDigit[offset]
            },
        )
    }
}
