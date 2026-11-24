package com.cauri.feature.onboarding.permissions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val IconBoxSize = 40.dp

// The separators start under the text, after the icon box and its gap.
private val SeparatorInset = CauriSpacing.l + IconBoxSize + CauriSpacing.m + 2.dp

/** Glass card listing what a permission is for, one [PermissionPoint] per line. */
@Composable
internal fun PermissionPoints(content: @Composable ColumnScope.() -> Unit) {
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = CauriSpacing.xs), content = content)
    }
}

/**
 * One thing a permission is for, with its lagoon icon.
 *
 * @param separated Whether a hairline separates it from the point above.
 */
@Composable
internal fun PermissionPoint(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes text: Int,
    separated: Boolean = true,
) {
    val palette = CauriTheme.palette
    if (separated) HorizontalDivider(modifier = Modifier.padding(start = SeparatorInset), color = palette.chip)
    Row(
        modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(IconBoxSize).background(palette.accentSoft, RoundedCornerShape(CauriRadius.icon)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = palette.accentText)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(text = stringResource(text), style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
    }
}
