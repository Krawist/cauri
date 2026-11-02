package com.cauri

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.cauri.core.design.theme.CauriTheme
import kotlin.time.Duration.Companion.seconds

/** How long the launch screen stays before the home screen or the onboarding opens. */
internal val LaunchDuration = 5.seconds

// Same tile as the system splash (res/drawable/splash_icon.xml), so the hand-off does not move.
private val TileSize = 96.dp
private val TileRadius = 26.dp
private val CoinSize = 62.dp
private val WordmarkGap = 14.dp
private val WordmarkHeight = 40.dp

// Background shapes, as fractions of the screen size, from the launch screen mockup.
private const val LAGOON_BLOB_X = 0.19f
private const val LAGOON_BLOB_Y = 0.34f
private const val GOLD_BLOB_X = 0.9f
private const val GOLD_BLOB_Y = 0.64f
private const val BLOB_RADIUS = 0.7f

/**
 * Launch screen: the logo tile in the centre of the screen, on the blurred lagoon and gold shapes, with the wordmark
 * under it. The shapes are radial gradients, so they look the same on every phone, with or without blur support.
 */
@Composable
internal fun LaunchScreen(modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .drawBehind {
                drawBlob(
                    color = palette.blobLagoon,
                    center = Offset(
                        size.width * LAGOON_BLOB_X,
                        size.height * LAGOON_BLOB_Y,
                    ),
                )
                drawBlob(color = palette.blobGold, center = Offset(size.width * GOLD_BLOB_X, size.height * GOLD_BLOB_Y))
            },
    ) {
        // A spacer as tall as the wordmark above the tile keeps the tile in the exact centre of the screen.
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WordmarkGap),
        ) {
            Spacer(Modifier.height(WordmarkHeight))
            Box(
                modifier = Modifier
                    .size(TileSize)
                    .background(palette.glass, RoundedCornerShape(TileRadius))
                    .border(1.dp, palette.glassBorder, RoundedCornerShape(TileRadius)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(com.cauri.core.design.R.drawable.logo_coin),
                    contentDescription = null,
                    modifier = Modifier.size(CoinSize),
                )
            }
            Box(modifier = Modifier.height(WordmarkHeight), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.launch_wordmark),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.045).em,
                    ),
                    color = palette.ink,
                )
            }
        }
    }
}

// A blurred disc, drawn as a radial gradient fading to transparent.
private fun DrawScope.drawBlob(color: Color, center: Offset) {
    val radius = size.width * BLOB_RADIUS
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LaunchScreenPreview() {
    CauriTheme { LaunchScreen() }
}
