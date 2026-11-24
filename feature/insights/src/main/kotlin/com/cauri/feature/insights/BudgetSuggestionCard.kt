package com.cauri.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.formatFcfa
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 40.dp

/** A budget Cauri suggests: its category and amount a month, « Accepter », « Ajuster » and « Ignorer ». */
@Composable
internal fun BudgetSuggestionCard(
    suggestion: BudgetSuggestion,
    onAccept: () -> Unit,
    onAdjust: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Text(
                text = suggestion.categoryName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(
                text = stringResource(R.string.insights_budget_suggested_amount, formatFcfa(suggestion.amount)),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
            Answers(onAccept = onAccept, onAdjust = onAdjust, onIgnore = onIgnore)
        }
    }
}

// « Accepter », « Ajuster » and « Ignorer », each felt its own way.
@Composable
private fun Answers(onAccept: () -> Unit, onAdjust: () -> Unit, onIgnore: () -> Unit) {
    val palette = CauriTheme.palette
    val haptics = rememberCauriHaptics()
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Button(
            onClick = {
                haptics.confirm()
                onAccept()
            },
            modifier = Modifier.height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.accent,
                contentColor = palette.onAccent,
            ),
        ) {
            Text(
                text = stringResource(R.string.insights_budget_accept),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        TextButton(onClick = onAdjust, modifier = Modifier.height(ButtonHeight)) {
            Text(
                text = stringResource(R.string.insights_budget_adjust),
                style = MaterialTheme.typography.labelLarge,
                color = palette.accentText,
            )
        }
        TextButton(
            onClick = {
                haptics.reject()
                onIgnore()
            },
            modifier = Modifier.height(ButtonHeight),
        ) {
            Text(
                text = stringResource(R.string.insights_budget_ignore),
                style = MaterialTheme.typography.labelLarge,
                color = palette.muted,
            )
        }
    }
}
