package com.cauri

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.cauri.core.design.component.BackdropShape
import com.cauri.core.design.component.backdrop
import com.cauri.core.design.theme.CauriTheme
import kotlin.time.Duration.Companion.seconds

/** How long the launch screen stays before the home screen or the onboarding opens. */
internal val LaunchDuration = 2.seconds

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
 * under it; [bottom] at the foot of the screen, such as the lock's « Déverrouiller » (CAU-57).
 */
@Composable
internal fun LaunchScreen(modifier: Modifier = Modifier, bottom: @Composable BoxScope.() -> Unit = {}) {
    val palette = CauriTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
            .backdrop(
                listOf(
                    BackdropShape(
                        color = palette.blobLagoon,
                        x = LAGOON_BLOB_X,
                        y = LAGOON_BLOB_Y,
                        radius = BLOB_RADIUS,
                    ),
                    BackdropShape(color = palette.blobGold, x = GOLD_BLOB_X, y = GOLD_BLOB_Y, radius = BLOB_RADIUS),
                ),
            ),
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
        bottom()
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LaunchScreenPreview() {
    CauriTheme { LaunchScreen() }
}
