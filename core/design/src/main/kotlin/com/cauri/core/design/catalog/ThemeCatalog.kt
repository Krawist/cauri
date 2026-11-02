package com.cauri.core.design.catalog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cauri.core.design.component.PlaceholderScreen
import com.cauri.core.design.theme.CauriPalette
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

// Catalogue of the design tokens and components, in light and dark mode. Preview only: token names are not user text.

@Composable
private fun CatalogSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun CatalogSurface(content: @Composable () -> Unit) {
    Surface(color = CauriTheme.palette.background) {
        Column(
            modifier = Modifier.padding(CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.xl),
        ) { content() }
    }
}

private fun CauriPalette.tokens(): List<Pair<String, Color>> = listOf(
    "background" to background,
    "ink" to ink,
    "muted" to muted,
    "glass" to glass,
    "glassStrong" to glassStrong,
    "glassBorder" to glassBorder,
    "field" to field,
    "sheet" to sheet,
    "segment" to segment,
    "chip" to chip,
    "track" to track,
    "trackStrong" to trackStrong,
    "handle" to handle,
    "scrim" to scrim,
    "accent" to accent,
    "onAccent" to onAccent,
    "accentSoft" to accentSoft,
    "accentText" to accentText,
    "gold" to gold,
    "goldSoft" to goldSoft,
    "goldText" to goldText,
    "coral" to coral,
    "coralSoft" to coralSoft,
    "coralText" to coralText,
    "greenSoft" to greenSoft,
    "greenText" to greenText,
    "blobLagoon" to blobLagoon,
    "blobGold" to blobGold,
    "blobCoral" to blobCoral,
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaletteCatalogPreview() {
    CauriTheme {
        CatalogSurface {
            CatalogSection(title = "Palette") {
                CauriTheme.palette.tokens().forEach { (name, color) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
                    ) {
                        Box(
                            Modifier
                                .size(CauriSpacing.touchTarget)
                                .border(1.dp, CauriTheme.palette.chip, RoundedCornerShape(CauriRadius.icon))
                                .background(color, RoundedCornerShape(CauriRadius.icon)),
                        )
                        Text(text = name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypographyCatalog() {
    val typography = MaterialTheme.typography
    listOf(
        "displayLarge" to typography.displayLarge,
        "displaySmall" to typography.displaySmall,
        "headlineLarge" to typography.headlineLarge,
        "headlineMedium" to typography.headlineMedium,
        "headlineSmall" to typography.headlineSmall,
        "titleLarge" to typography.titleLarge,
        "titleMedium" to typography.titleMedium,
        "titleSmall" to typography.titleSmall,
        "bodyLarge" to typography.bodyLarge,
        "bodyMedium" to typography.bodyMedium,
        "bodySmall" to typography.bodySmall,
        "labelLarge" to typography.labelLarge,
        "labelMedium" to typography.labelMedium,
        "labelSmall" to typography.labelSmall,
    ).forEach { (name, style) ->
        Text(text = "$name 1 234 567", style = style, color = CauriTheme.palette.ink)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TypographyCatalogPreview() {
    CauriTheme {
        CatalogSurface {
            CatalogSection(title = "Typography") { TypographyCatalog() }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShapeAndSpacingCatalogPreview() {
    val radii: List<Pair<String, Dp>> = listOf(
        "hero" to CauriRadius.hero,
        "card" to CauriRadius.card,
        "group" to CauriRadius.group,
        "tile" to CauriRadius.tile,
        "control" to CauriRadius.control,
        "icon" to CauriRadius.icon,
        "field" to CauriRadius.field,
    )
    val spacings: List<Pair<String, Dp>> = listOf(
        "xs" to CauriSpacing.xs,
        "s" to CauriSpacing.s,
        "m" to CauriSpacing.m,
        "l" to CauriSpacing.l,
        "xl" to CauriSpacing.xl,
        "xxl" to CauriSpacing.xxl,
        "touchTarget" to CauriSpacing.touchTarget,
    )
    CauriTheme {
        CatalogSurface {
            CatalogSection(title = "Radii") {
                radii.forEach { (name, radius) ->
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                            .border(1.dp, CauriTheme.palette.glassBorder, RoundedCornerShape(radius))
                            .background(CauriTheme.palette.accentSoft, RoundedCornerShape(radius)),
                        contentAlignment = Alignment.Center,
                    ) { Text(text = "$name $radius", style = MaterialTheme.typography.labelLarge) }
                }
            }
            CatalogSection(title = "Spacing") {
                spacings.forEach { (name, spacing) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
                    ) {
                        Box(Modifier.size(width = spacing, height = 16.dp).background(CauriTheme.palette.gold))
                        Text(text = "$name $spacing", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentCatalogPreview() {
    CauriTheme {
        PlaceholderScreen(title = "Compter mon cash", subtitle = "Écran à venir", actionLabel = "Retour")
    }
}
