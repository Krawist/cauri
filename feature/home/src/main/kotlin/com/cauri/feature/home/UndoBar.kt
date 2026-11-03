package com.cauri.feature.home

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
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import kotlinx.coroutines.delay

private val BarHeight = 52.dp
private val BarElevation = 12.dp

// Long enough to read and tap « Annuler », short enough not to linger.
private const val UNDO_MILLIS = 4_000L

/** « Moto · 500 F enregistré » with « Annuler », for a few seconds after a tap on a habit. */
@Composable
internal fun UndoBar(
    recorded: RecordedHabit,
    onUndo: () -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = CauriTheme.palette
    LaunchedEffect(recorded) {
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
                painter = painterResource(DesignR.drawable.ic_check),
                contentDescription = null,
                tint = palette.greenText,
            )
            Text(
                text = stringResource(R.string.home_habit_recorded, recorded.name, formatFcfa(recorded.amount)),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = palette.ink,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onUndo, colors = ButtonDefaults.textButtonColors(contentColor = palette.accentText)) {
                Text(
                    text = stringResource(R.string.home_undo),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}
