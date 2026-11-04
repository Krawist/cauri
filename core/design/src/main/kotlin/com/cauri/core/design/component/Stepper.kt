package com.cauri.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val StepButtonSize = 32.dp
private val ValueWidth = 20.dp

/** « − 4 + »: [value] between two round buttons, each a step of one; a bound reached greys its button. */
@Composable
fun Stepper(value: Int, canLower: Boolean, canRaise: Boolean, onStep: (Int) -> Unit, label: String? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s), verticalAlignment = Alignment.CenterVertically) {
        StepButton(
            sign = "−",
            description = stringResource(R.string.stepper_less),
            enabled = canLower,
            onClick = { onStep(-1) },
        )
        Text(
            text = label ?: value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = CauriTheme.palette.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = ValueWidth),
        )
        StepButton(
            sign = "+",
            description = stringResource(R.string.stepper_more),
            enabled = canRaise,
            onClick = { onStep(1) },
        )
    }
}

@Composable
private fun StepButton(sign: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Box(
        modifier = Modifier
            .size(StepButtonSize)
            .pressScale()
            .background(palette.chip, CircleShape)
            .clickable(enabled = enabled, role = Role.Button) {
                haptics.key()
                onClick()
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sign,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) palette.ink else palette.muted,
        )
    }
}
