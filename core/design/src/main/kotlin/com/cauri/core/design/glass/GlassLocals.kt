package com.cauri.core.design.glass

import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState

/** Whether glass surfaces blur, chosen once at startup and provided by `CauriTheme`. */
internal val LocalGlassBlur = staticCompositionLocalOf { false }

/** What glass surfaces blur, provided by [GlassScreen]; `null` outside of one. */
internal val LocalGlassHazeState = staticCompositionLocalOf<HazeState?> { null }
