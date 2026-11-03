package com.cauri.feature.places

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cauri.core.design.theme.CauriTheme

/**
 * Where the map of the mockups goes: drawn streets for now, by decision, since a real map would load its tiles from the
 * network. [pins] are placed on it; [circle], when given, is the share of the width the radius around the place takes,
 * with the pin of the place in its middle. A real map can take its place later with the same parameters.
 */
@Composable
internal fun MapArea(modifier: Modifier = Modifier, pins: List<MapPin> = emptyList(), circle: Float? = null) {
    val palette = CauriTheme.palette
    Canvas(modifier = modifier) {
        drawRect(palette.field)
        streets(strong = palette.trackStrong, light = palette.track, park = palette.greenSoft)
        pins.forEach {
            val centre = Offset(it.x * size.width, it.y * size.height)
            drawCircle(if (it.highlighted) palette.gold else palette.muted, radius = 9.dp.toPx(), center = centre)
            drawCircle(Color.White, radius = 3.5.dp.toPx(), center = centre)
        }
        if (circle != null) {
            val radius = circle * size.width
            drawCircle(palette.accentSoft, radius = radius, center = center)
            drawCircle(
                palette.accent,
                radius = radius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx())),
                ),
            )
            drawCircle(Color.White, radius = 11.5.dp.toPx(), center = center)
            drawCircle(palette.gold, radius = 10.dp.toPx(), center = center)
        }
    }
}

// The streets and parks of the mockups, drawn on a 350 × 150 grid stretched to the area.
@Suppress("MagicNumber") // The coordinates of the mockups' drawing.
private fun DrawScope.streets(strong: Color, light: Color, park: Color) {
    val x = size.width / GRID_WIDTH
    val y = size.height / GRID_HEIGHT
    fun road(color: Color, width: Float, build: Path.() -> Unit) = drawPath(
        path = Path().apply(build),
        color = color,
        style = Stroke(width = width.dp.toPx(), cap = StrokeCap.Round),
    )
    road(strong, width = 14f) {
        moveTo(-10 * x, 93 * y)
        cubicTo(80 * x, 78 * y, 170 * x, 112 * y, 360 * x, 72 * y)
    }
    road(strong, width = 10f) {
        moveTo(120 * x, -10 * y)
        cubicTo(130 * x, 60 * y, 110 * x, 105 * y, 140 * x, 160 * y)
    }
    road(light, width = 8f) {
        moveTo(230 * x, -10 * y)
        lineTo(260 * x, 160 * y)
    }
    road(light, width = 6f) {
        moveTo(-10 * x, 30 * y)
        lineTo(360 * x, 45 * y)
    }
    val corner = CornerRadius(6.dp.toPx())
    drawRoundRect(park, topLeft = Offset(160 * x, 12 * y), size = Size(54 * x, 30 * y), cornerRadius = corner)
    drawRoundRect(park, topLeft = Offset(20 * x, 108 * y), size = Size(70 * x, 30 * y), cornerRadius = corner)
}

private const val GRID_WIDTH = 350f
private const val GRID_HEIGHT = 150f
