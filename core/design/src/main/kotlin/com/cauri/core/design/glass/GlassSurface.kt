package com.cauri.core.design.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cauri.core.design.theme.CauriTheme
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

private val GlassBlurRadius = 24.dp
private val GlassBorderWidth = 1.dp

/**
 * Translucent "glass" surface for cards, the bottom bar and sheets: it blurs what is behind it inside a [GlassScreen].
 * Where blur is off (see `supportsGlassBlur`), or outside a [GlassScreen], it is an opaque surface tinted with the same
 * glass colour, with the same shape and border.
 *
 * @param modifier Layout of the surface (size, padding, alignment), applied outside its shape.
 * @param strong Whether to use the more opaque glass of the bottom bar.
 * @param elevation Height of the soft shadow under the surface, for surfaces that float above the content.
 * @param interaction What makes the surface react to touch (`clickable`, `selectable`…), applied inside its shape so
 * that the press feedback follows the corners. Passed in [modifier] instead, the feedback would be a rectangle.
 */
@Composable
fun GlassSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    strong: Boolean = false,
    elevation: Dp = 0.dp,
    interaction: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = CauriTheme.palette
    val tint = if (strong) palette.glassStrong else palette.glass
    val hazeState = LocalGlassHazeState.current
    val fill = if (LocalGlassBlur.current && hazeState != null) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = palette.background,
                tint = HazeTint(tint),
                blurRadius = GlassBlurRadius,
                noiseFactor = 0f,
            ),
        )
    } else {
        Modifier.background(tint.compositeOver(palette.background))
    }
    Box(
        modifier = modifier
            // The fill is opaque (blurred backdrop or tinted fallback), so the shadow only shows around the surface.
            .shadow(elevation = elevation, shape = shape, ambientColor = palette.shadow, spotColor = palette.shadow)
            .clip(shape)
            .then(interaction)
            .then(fill)
            .border(width = GlassBorderWidth, color = palette.glassBorder, shape = shape),
        content = content,
    )
}
