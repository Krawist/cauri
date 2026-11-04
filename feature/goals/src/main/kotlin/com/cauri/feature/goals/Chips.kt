package com.cauri.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ChipHeight = 40.dp
private val SelectedBorderWidth = 1.5.dp

/** Glass chips, one per option of [options] (its value and label), the [selected] one in lagoon. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> Chips(options: List<Pair<T, String>>, selected: T?, onSelect: (T) -> Unit) {
    val palette = CauriTheme.palette
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        options.forEach { (value, label) ->
            val chosen = value == selected
            GlassSurface(
                shape = CircleShape,
                modifier = Modifier
                    .height(ChipHeight)
                    .then(if (chosen) Modifier.border(SelectedBorderWidth, palette.accent, CircleShape) else Modifier),
                interaction = Modifier.selectable(selected = chosen, role = Role.RadioButton, onClick = {
                    onSelect(value)
                }),
            ) {
                if (chosen) Box(Modifier.matchParentSize().background(palette.accentSoft))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (chosen) palette.accentText else palette.ink,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = CauriSpacing.l),
                )
            }
        }
    }
}
