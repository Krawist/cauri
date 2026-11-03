package com.cauri.feature.cash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val RowMinHeight = 68.dp
private val FieldHeight = 40.dp
private val FieldTextWidth = 72.dp

/** A line of the gap split: its name, where it comes from, and its amount, the hint grey until typed. */
@Composable
internal fun GapLineRow(line: GapLine, onAmountChange: (String) -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = RowMinHeight).padding(vertical = CauriSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = line.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(
                text = when (line.source) {
                    GapLineSource.HABIT -> stringResource(R.string.cash_split_from_habit)
                    GapLineSource.VISIT -> stringResource(R.string.cash_split_from_visit, line.visitMinutes ?: 0)
                    GapLineSource.ADDED -> stringResource(R.string.cash_split_added)
                },
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
        AmountField(line = line, onAmountChange = onAmountChange)
    }
}

// Right-aligned amount on a soft field, lagoon once typed.
@Composable
private fun AmountField(line: GapLine, onAmountChange: (String) -> Unit) {
    val palette = CauriTheme.palette
    val description = stringResource(R.string.cash_split_amount_of, line.name)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    Row(
        modifier = Modifier
            .height(FieldHeight)
            .background(
                if (line.amount.isEmpty()) palette.field else palette.accentSoft,
                RoundedCornerShape(CauriRadius.field),
            )
            .padding(horizontal = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = line.amount,
            onValueChange = onAmountChange,
            singleLine = true,
            textStyle = textStyle.copy(color = palette.ink),
            cursorBrush = SolidColor(palette.accent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            visualTransformation = FcfaVisualTransformation,
            modifier = Modifier.width(FieldTextWidth).semantics { contentDescription = description },
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (line.amount.isEmpty()) {
                        Text(
                            text = line.hint?.let { stringResource(R.string.cash_split_hint, formatFcfa(it)) } ?: "?",
                            style = textStyle.copy(fontWeight = FontWeight.Medium),
                            color = palette.muted,
                        )
                    }
                    field()
                }
            },
        )
        Text(
            text = stringResource(R.string.cash_currency),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
}
