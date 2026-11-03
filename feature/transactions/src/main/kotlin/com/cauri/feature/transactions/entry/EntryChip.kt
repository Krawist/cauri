package com.cauri.feature.transactions.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/** Height of a chip, to lay out a number of lines of them. */
internal val EntryChipHeight = 40.dp
private val SelectedBorderWidth = 1.5.dp

/**
 * A choice of the quick entry, an account or a category: a pill, outlined in [color] and tinted with [soft] when
 * [selected].
 */
@Composable
internal fun EntryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CauriTheme.palette.accent,
    soft: Color = CauriTheme.palette.accentSoft,
    text: Color = CauriTheme.palette.accentText,
) {
    val palette = CauriTheme.palette
    Box(
        modifier = modifier
            .heightIn(min = EntryChipHeight)
            .clip(CircleShape)
            .background(if (selected) soft else palette.chip, CircleShape)
            .then(if (selected) Modifier.border(SelectedBorderWidth, color, CircleShape) else Modifier)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = CauriSpacing.l),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = if (selected) text else palette.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
