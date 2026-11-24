package com.cauri.feature.insights

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val IconBoxSize = 40.dp

/** A card of « Analyses » telling one thing: its icon in [tint] on [fill], a [title] and a [text]. */
@Composable
internal fun AdviceCard(@DrawableRes icon: Int, tint: Color, fill: Color, title: String, text: String) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(CauriSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        ) {
            Box(
                modifier = Modifier.size(IconBoxSize).background(fill, RoundedCornerShape(CauriRadius.icon)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = tint)
            }
            Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                )
                Text(text = text, style = MaterialTheme.typography.bodySmall, color = palette.muted)
            }
        }
    }
}
