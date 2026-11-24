package com.cauri.core.design.catalog

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.krawist.design.glass.GlassScreen
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

// Glass surfaces over coloured shapes, with blur and with the opaque fallback. Preview only.

@Composable
private fun GlassCatalog() {
    GlassScreen(
        modifier = Modifier.height(260.dp),
        background = {
            Box(
                Modifier
                    .padding(start = CauriSpacing.xl, top = CauriSpacing.xl)
                    .size(140.dp)
                    .background(CauriTheme.palette.blobLagoon, CircleShape),
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(140.dp)
                    .background(CauriTheme.palette.blobGold, CircleShape),
            )
        },
        overlay = {
            GlassSurface(
                shape = CircleShape,
                strong = true,
                modifier = Modifier.align(
                    Alignment.BottomCenter,
                ).padding(CauriSpacing.l).size(width = 240.dp, height = 68.dp),
            ) {}
        },
    ) {
        GlassSurface(
            shape = RoundedCornerShape(CauriRadius.hero),
            modifier = Modifier.padding(CauriSpacing.xl).size(width = 260.dp, height = 120.dp),
        ) {
            Text(
                text = "glass",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(CauriSpacing.xl),
            )
        }
    }
}

@Preview(name = "Light, blur")
@Preview(name = "Dark, blur", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GlassBlurCatalogPreview() {
    CauriTheme(glassBlur = true) { GlassCatalog() }
}

@Preview(name = "Light, opaque fallback")
@Preview(name = "Dark, opaque fallback", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GlassFallbackCatalogPreview() {
    CauriTheme(glassBlur = false) { GlassCatalog() }
}
