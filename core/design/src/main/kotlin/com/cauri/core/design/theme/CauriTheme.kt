package com.cauri.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Cauri theme. Follows the phone's dark mode; screens read the full palette through [CauriTheme.palette]. */
@Composable
fun CauriTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            secondary = palette.gold,
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
            secondary = palette.gold,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            error = palette.coralText,
        )
    }
    CompositionLocalProvider(LocalCauriPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = CauriTypography, content = content)
    }
}

object CauriTheme {
    val palette: CauriPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalCauriPalette.current
}
