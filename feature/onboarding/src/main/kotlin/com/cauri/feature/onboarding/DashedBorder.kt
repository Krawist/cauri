package com.cauri.feature.onboarding

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DashWidth = 6.dp
private val DashedBorderWidth = 1.5.dp

/** Dashed border of the « + Ajouter » actions, as in the mockups; [radius] `null` draws a pill. */
internal fun Modifier.dashedBorder(color: Color, radius: Dp? = null): Modifier = drawBehind {
    val dash = DashWidth.toPx()
    val corner = radius?.toPx() ?: (size.height / 2)
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(corner),
        style = Stroke(
            width = DashedBorderWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
        ),
    )
}
