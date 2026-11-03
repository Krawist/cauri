package com.cauri.core.design.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

/**
 * Widens the content by [margin] on both sides, out of the screen's margins, such as a row of cards that scrolls from
 * edge to edge; the content then pads itself by [margin] to start aligned with the rest of the screen.
 */
fun Modifier.bleed(margin: Dp) = layout { measurable, constraints ->
    val extra = margin.roundToPx() * 2
    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}
