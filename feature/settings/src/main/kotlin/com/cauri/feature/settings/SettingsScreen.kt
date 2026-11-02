package com.cauri.feature.settings

import android.content.Intent
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionStatus
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val IconBoxSize = 40.dp

// The separators start under the text, after the icon box and its gap.
private val SeparatorInset = CauriSpacing.l + IconBoxSize + CauriSpacing.m

/** Settings: each permission with its status; a tap opens the system screen where it is changed. */
@Composable
internal fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    val open = { intent: Intent -> context.startActivity(intent) }
    SettingsContent(
        status = status,
        onBack = onBack,
        onNotificationAccess = { open(viewModel.notificationAccessSettings()) },
        onNotifications = { open(viewModel.notificationSettings()) },
        onLocation = { open(viewModel.locationSettings()) },
    )
}

@Composable
private fun SettingsContent(
    status: PermissionStatus,
    onBack: () -> Unit,
    onNotificationAccess: () -> Unit,
    onNotifications: () -> Unit,
    onLocation: () -> Unit,
) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
    ) {
        BackButton(onClick = onBack)
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
            modifier = Modifier.padding(top = CauriSpacing.xl, bottom = CauriSpacing.xxl),
        )
        Text(
            text = stringResource(R.string.settings_permissions),
            style = MaterialTheme.typography.labelMedium,
            color = palette.muted,
            modifier = Modifier.padding(start = CauriSpacing.xs, bottom = CauriSpacing.s),
        )
        PermissionsCard(
            status = status,
            onNotificationAccess = onNotificationAccess,
            onNotifications = onNotifications,
            onLocation = onLocation,
        )
        Text(
            text = stringResource(R.string.settings_permissions_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.padding(start = CauriSpacing.xs, top = CauriSpacing.s, end = CauriSpacing.xs),
        )
    }
}

@Composable
private fun PermissionsCard(
    status: PermissionStatus,
    onNotificationAccess: () -> Unit,
    onNotifications: () -> Unit,
    onLocation: () -> Unit,
) {
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column {
            PermissionRow(
                icon = DesignR.drawable.ic_message,
                title = R.string.settings_notification_access,
                text = R.string.settings_notification_access_text,
                status = if (status.notificationAccess) granted(R.string.settings_allowed) else refused(),
                onClick = onNotificationAccess,
            )
            Separator()
            PermissionRow(
                icon = DesignR.drawable.ic_bell,
                title = R.string.settings_notifications,
                text = R.string.settings_notifications_text,
                status = if (status.notificationsAllowed) {
                    granted(R.string.settings_allowed)
                } else {
                    refused()
                },
                onClick = onNotifications,
            )
            Separator()
            PermissionRow(
                icon = DesignR.drawable.ic_place,
                title = R.string.settings_location,
                text = R.string.settings_location_text,
                status = when (status.location) {
                    LocationAccess.ALWAYS -> granted(R.string.settings_location_always)
                    LocationAccess.WHILE_IN_USE -> off(R.string.settings_location_while_in_use)
                    LocationAccess.DENIED -> off(R.string.settings_off)
                },
                onClick = onLocation,
            )
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
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

@Composable
private fun PermissionRow(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes text: Int,
    status: StatusPill,
    onClick: () -> Unit,
) {
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
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(text = stringResource(text), style = MaterialTheme.typography.bodySmall, color = palette.muted)
        }
        Text(
            text = stringResource(status.label),
            style = MaterialTheme.typography.labelMedium,
            color = status.color,
            modifier = Modifier
                .background(status.fill, CircleShape)
                .padding(horizontal = CauriSpacing.s + 2.dp, vertical = CauriSpacing.xs),
        )
        Icon(painter = painterResource(DesignR.drawable.ic_chevron), contentDescription = null, tint = palette.muted)
    }
}

@Composable
private fun Separator() {
    HorizontalDivider(modifier = Modifier.padding(start = SeparatorInset), color = CauriTheme.palette.chip)
}

@Composable
private fun granted(@StringRes label: Int) =
    StatusPill(label = label, fill = CauriTheme.palette.greenSoft, color = CauriTheme.palette.greenText)

// Notifications are what Cauri relies on: a refusal is shown in coral.
@Composable
private fun refused() = StatusPill(
    label = R.string.settings_refused,
    fill = CauriTheme.palette.coralSoft,
    color = CauriTheme.palette.coralText,
)

// The location is optional: without it, the status stays neutral.
@Composable
private fun off(@StringRes label: Int) =
    StatusPill(label = label, fill = CauriTheme.palette.chip, color = CauriTheme.palette.muted)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            SettingsContent(
                status = PermissionStatus(
                    notificationAccess = true,
                    notificationsAllowed = false,
                    location = LocationAccess.WHILE_IN_USE,
                ),
                onBack = {},
                onNotificationAccess = {},
                onNotifications = {},
                onLocation = {},
            )
        }
    }
}
