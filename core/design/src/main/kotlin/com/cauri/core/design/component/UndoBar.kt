package com.cauri.core.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import kotlinx.coroutines.delay

private val BarHeight = 52.dp
private val BarElevation = 12.dp

// Long enough to read and tap « Annuler », short enough not to linger.
private const val UNDO_MILLIS = 4_000L

/**
 * [message], such as « Moto · 500 F enregistré », with « Annuler », for a few seconds after an action the user can
 * undo; a new [message] starts the time again.
 */
@Composable
fun UndoBar(message: String, onUndo: () -> Unit, onDismissed: () -> Unit, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    LaunchedEffect(message) {
        delay(UNDO_MILLIS)
        onDismissed()
    }
    GlassSurface(
        shape = CircleShape,
        modifier = modifier.fillMaxWidth().height(BarHeight),
        strong = true,
        elevation = BarElevation,
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(start = CauriSpacing.l, end = CauriSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = palette.greenText,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.ink,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onUndo, colors = ButtonDefaults.textButtonColors(contentColor = palette.accentText)) {
                Text(
                    text = stringResource(R.string.undo),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}
