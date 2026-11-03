package com.cauri.core.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val KeyHeight = 56.dp
private val KeyShape = RoundedCornerShape(16.dp)

// The keys row by row, « 000 » left of « 0 » as in the mockup; the last key erases.
private val Rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("000", "0"))

/**
 * Numeric keypad of the amount: [onDigits] gets a digit or « 000 », [onDelete] erases the last digit, and a long press
 * on the erase key clears the whole amount through [onClear].
 */
@Composable
fun Keypad(onDigits: (String) -> Unit, onDelete: () -> Unit, onClear: () -> Unit) {
    // Each key answers with the light tap of a keyboard.
    val haptics = rememberCauriHaptics()
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Rows.forEach { keys ->
            Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
                keys.forEach { key ->
                    DigitKey(label = key, onClick = {
                        haptics.key()
                        onDigits(key)
                    })
                }
                if (keys.size < Rows.first().size) {
                    DeleteKey(
                        onClick = {
                            haptics.key()
                            onDelete()
                        },
                        onLongClick = {
                            haptics.hold()
                            onClear()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DigitKey(label: String, onClick: () -> Unit) {
    GlassSurface(
        shape = KeyShape,
        // Each key sinks a little under the finger.
        modifier = Modifier.weight(1f).height(KeyHeight).pressScale(),
        // Stronger than the cards: each key stands out on the pale shapes of the light mode.
        strong = true,
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = CauriTheme.palette.ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun RowScope.DeleteKey(onClick: () -> Unit, onLongClick: () -> Unit) {
    val description = stringResource(R.string.keypad_delete)
    Box(
        modifier = Modifier
            .weight(1f)
            .height(KeyHeight)
            .pressScale()
            .semantics { contentDescription = description }
            .combinedClickable(
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.keypad_clear),
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_backspace),
            contentDescription = null,
            tint = CauriTheme.palette.ink,
        )
    }
}
