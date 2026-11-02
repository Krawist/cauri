package com.cauri.core.design.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cauri.core.design.theme.CauriTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

// A glass surface blurs the layers below the nearest one that contains it.
private const val BACKGROUND_LAYER = 0f
private const val CONTENT_LAYER = 1f

/**
 * Screen that holds what its glass surfaces blur, in three layers:
 * - [background]: the blurred shapes behind everything;
 * - [content]: the screen content, often a scrolling list; its glass cards blur the [background] only;
 * - [overlay]: what floats above the content (bottom bar, sheets); its glass blurs the [background] and the [content].
 */
@Composable
fun GlassScreen(
    modifier: Modifier = Modifier,
    background: @Composable BoxScope.() -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val hazeState = remember { HazeState() }
    CompositionLocalProvider(LocalGlassHazeState provides hazeState) {
        Box(modifier = modifier.fillMaxSize().background(CauriTheme.palette.background)) {
            Box(
                modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = BACKGROUND_LAYER),
                content = background,
            )
            Box(
                modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = CONTENT_LAYER),
                content = content,
            )
            overlay()
        }
    }
}
