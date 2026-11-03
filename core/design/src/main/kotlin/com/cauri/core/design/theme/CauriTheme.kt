package com.cauri.core.design.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.cauri.core.design.glass.LocalGlassBlur

/**
 * Cauri theme. Follows the phone's dark mode; screens read the full palette through [CauriTheme.palette].
 *
 * @param glassBlur Whether glass surfaces blur what is behind them, from `supportsGlassBlur()` at startup; otherwise
 * they are opaque tinted surfaces.
 */
@Composable
fun CauriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    glassBlur: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            secondary = palette.accent,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            error = palette.coralText,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            secondary = palette.accent,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            error = palette.coralText,
        )
    }
    val context = LocalContext.current
    // « Supprimer les animations » sets the animator scale to 0: Cauri then moves nothing.
    val motion = remember(context) {
        CauriMotion(
            reduced =
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f,
        )
    }
    CompositionLocalProvider(
        LocalCauriPalette provides palette,
        LocalGlassBlur provides glassBlur,
        LocalCauriMotion provides motion,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = CauriTypography, content = content)
    }
}

object CauriTheme {
    val palette: CauriPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalCauriPalette.current

    /** The durations, easings and transitions of every animation. */
    val motion: CauriMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalCauriMotion.current
}

internal val LocalCauriMotion = staticCompositionLocalOf { CauriMotion(reduced = false) }
