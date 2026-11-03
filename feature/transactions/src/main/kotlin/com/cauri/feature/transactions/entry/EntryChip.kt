package com.cauri.feature.transactions.entry

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cauri.core.design.component.pressScale
import com.cauri.core.design.component.rememberCauriHaptics
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
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    val motion = CauriTheme.motion
    // The selection glides in, rather than jumping from a chip to the other.
    val fill by animateColorAsState(if (selected) soft else palette.chip, motion.tween(), label = "chip fill")
    val border by animateColorAsState(
        if (selected) color else color.copy(alpha = 0f),
        motion.tween(),
        label = "chip border",
    )
    Box(
        modifier = modifier
            .heightIn(min = EntryChipHeight)
            .pressScale()
            .clip(CircleShape)
            .background(fill, CircleShape)
            .border(SelectedBorderWidth, border, CircleShape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = {
                haptics.select()
                onClick()
            })
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
