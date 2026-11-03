package com.cauri.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val BorderWidth = 1.dp
private val CloseSize = 32.dp

/**
 * A help bubble about the element next to it, in lagoon: [text] and a cross. It shows the first time the element is on
 * screen, one bubble at a time ([LocalHelpBubbles]), until the user closes it; then never again. [id] names it.
 */
@Composable
fun HelpBubble(id: String, text: String, modifier: Modifier = Modifier) {
    val bubbles = LocalHelpBubbles.current
    DisposableEffect(id, bubbles) {
        bubbles.request(id)
        onDispose { bubbles.release(id) }
    }
    AnimatedPresence(
        value = text.takeIf { bubbles.isShown(id) },
        enter = CauriTheme.motion.itemEnter(),
        exit = CauriTheme.motion.itemExit(),
        modifier = modifier,
    ) { shown ->
        Bubble(text = shown, onClose = { bubbles.close(id) })
    }
}

@Composable
private fun Bubble(text: String, onClose: () -> Unit) {
    val palette = CauriTheme.palette
    val haptics = rememberCauriHaptics()
    val shape = RoundedCornerShape(CauriRadius.tile)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.accentSoft, shape)
            .border(BorderWidth, palette.accent, shape)
            .padding(start = CauriSpacing.l, top = CauriSpacing.s, bottom = CauriSpacing.s, end = CauriSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = palette.accentText,
            modifier = Modifier.weight(1f),
        )
        val description = stringResource(R.string.help_close)
        Box(
            modifier = Modifier
                .size(CloseSize)
                .clickable(onClickLabel = description, role = Role.Button) {
                    haptics.select()
                    onClose()
                }
                .background(palette.chip, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_help_close),
                contentDescription = description,
                tint = palette.accentText,
            )
        }
    }
}
