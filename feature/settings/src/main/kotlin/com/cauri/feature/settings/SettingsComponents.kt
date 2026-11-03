package com.cauri.feature.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val IconBoxSize = 40.dp

// The separators start under the text, after the icon box and its gap.
private val SeparatorInset = CauriSpacing.l + IconBoxSize + CauriSpacing.m

/** Round glass back button at the top of a settings screen. */
@Composable
internal fun BackButton(onClick: () -> Unit) {
    val description = stringResource(R.string.settings_back)
    GlassSurface(
        shape = CircleShape,
        modifier = Modifier.size(CauriSpacing.touchTarget),
        interaction = Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_back),
            contentDescription = description,
            tint = CauriTheme.palette.ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

/** Small caption above a group of rows. */
@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = CauriTheme.palette.muted,
        modifier = modifier.padding(start = CauriSpacing.xs, bottom = CauriSpacing.s),
    )
}

/** Glass card grouping rows, separated by [Separator]. */
@Composable
internal fun RowGroup(content: @Composable () -> Unit) {
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column { content() }
    }
}

/** A setting with its lagoon icon and its status pill; a tap opens the system screen where it is changed. */
@Composable
internal fun SettingRow(@DrawableRes icon: Int, title: String, text: String, status: StatusPill, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(IconBoxSize).background(palette.accentSoft, RoundedCornerShape(CauriRadius.icon)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = palette.accentText)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(text = text, style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
        Text(
            text = stringResource(status.label),
            style = MaterialTheme.typography.labelMedium,
            color = status.color,
            modifier = Modifier
                .background(status.fill, CircleShape)
                .padding(horizontal = CauriSpacing.s + 2.dp, vertical = CauriSpacing.xs),
        )
        if (onClick != null) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_chevron),
                contentDescription = null,
                tint = palette.muted,
            )
        }
    }
}

/** Hairline between two rows of a [RowGroup]. */
@Composable
internal fun Separator() {
    HorizontalDivider(modifier = Modifier.padding(start = SeparatorInset), color = CauriTheme.palette.chip)
}

/** Green status: the setting helps Cauri. */
@Composable
internal fun granted(@StringRes label: Int) =
    StatusPill(label = label, fill = CauriTheme.palette.greenSoft, color = CauriTheme.palette.greenText)

/** Coral status: Cauri misses what it relies on. */
@Composable
internal fun refused(@StringRes label: Int = R.string.settings_refused) =
    StatusPill(label = label, fill = CauriTheme.palette.coralSoft, color = CauriTheme.palette.coralText)

/** Lagoon status: a setting the user turned on. */
@Composable
internal fun active(@StringRes label: Int) =
    StatusPill(label = label, fill = CauriTheme.palette.accentSoft, color = CauriTheme.palette.accentText)

/** Neutral status, for what is optional, such as the location. */
@Composable
internal fun off(@StringRes label: Int) =
    StatusPill(label = label, fill = CauriTheme.palette.chip, color = CauriTheme.palette.muted)
