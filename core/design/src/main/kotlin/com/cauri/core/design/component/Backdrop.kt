package com.cauri.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.cauri.core.design.theme.CauriTheme

// Shapes behind the screens, as on the home screen mockup: gold at the top left, lagoon on the right, coral at the
// bottom.
private const val GOLD_X = 0.13f
private const val GOLD_Y = 0.27f
private const val LAGOON_X = 0.95f
private const val LAGOON_Y = 0.52f
private const val CORAL_X = 0.44f
private const val CORAL_Y = 0.92f
private const val SHAPE_RADIUS = 0.6f

/**
 * Draws the blurred [shapes] behind the content. They are radial gradients, so they look the same on every phone,
 * with or without blur support.
 */
fun Modifier.backdrop(shapes: List<BackdropShape>): Modifier = drawBehind {
    shapes.forEach { shape ->
        val center = Offset(size.width * shape.x, size.height * shape.y)
        val radius = size.width * shape.radius
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(shape.color, shape.color.copy(alpha = 0f)),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}

/** The shapes drawn behind the screens of the app, in the current mode's colours. */
@Composable
@ReadOnlyComposable
fun screenBackdropShapes(): List<BackdropShape> {
    val palette = CauriTheme.palette
    return listOf(
        BackdropShape(color = palette.blobGold, x = GOLD_X, y = GOLD_Y, radius = SHAPE_RADIUS),
        BackdropShape(color = palette.blobLagoon, x = LAGOON_X, y = LAGOON_Y, radius = SHAPE_RADIUS),
        BackdropShape(color = palette.blobCoral, x = CORAL_X, y = CORAL_Y, radius = SHAPE_RADIUS),
    )
}
