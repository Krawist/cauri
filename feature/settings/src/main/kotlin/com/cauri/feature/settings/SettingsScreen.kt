package com.cauri.feature.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionStatus
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/** Settings: each permission and the battery with their status; a tap opens the system screen where it is changed. */
@Composable
internal fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    // Named apart from the action it backs, which shares its name.
    val back = onBack
    val actions = remember(viewModel, context, back) {
        object : SettingsActions {
            override fun onBack() = back()

            override fun onNotificationAccess() = context.startActivity(viewModel.notificationAccessSettings())

            override fun onNotifications() = context.startActivity(viewModel.notificationSettings())

            override fun onLocation() = context.startActivity(viewModel.locationSettings())

            override fun onBattery() = context.startActivity(viewModel.batterySettings())
        }
    }
    SettingsContent(state = state, actions = actions)
}

@Composable
private fun SettingsContent(state: SettingsUiState, actions: SettingsActions) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
    ) {
        BackButton(onClick = actions::onBack)
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
            modifier = Modifier.padding(top = CauriSpacing.xl, bottom = CauriSpacing.xxl),
        )
        SectionLabel(stringResource(R.string.settings_permissions))
        PermissionsGroup(status = state.permissions, actions = actions)
        SectionLabel(
            stringResource(R.string.settings_battery_section),
            modifier = Modifier.padding(top = CauriSpacing.xxl),
        )
        RowGroup {
            SettingRow(
                icon = DesignR.drawable.ic_battery,
                title = R.string.settings_battery,
                text = R.string.settings_battery_text,
                status = if (state.batteryUnrestricted) {
                    granted(R.string.settings_battery_unrestricted)
                } else {
                    refused(R.string.settings_battery_restricted)
                },
                onClick = actions::onBattery,
            )
        }
        Text(
            text = stringResource(R.string.settings_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
            modifier = Modifier.padding(start = CauriSpacing.xs, top = CauriSpacing.s, end = CauriSpacing.xs),
        )
    }
}

@Composable
private fun PermissionsGroup(status: PermissionStatus, actions: SettingsActions) {
    RowGroup {
        SettingRow(
            icon = DesignR.drawable.ic_message,
            title = R.string.settings_notification_access,
            text = R.string.settings_notification_access_text,
            status = if (status.notificationAccess) granted(R.string.settings_allowed) else refused(),
            onClick = actions::onNotificationAccess,
        )
        Separator()
        SettingRow(
            icon = DesignR.drawable.ic_bell,
            title = R.string.settings_notifications,
            text = R.string.settings_notifications_text,
            status = if (status.notificationsAllowed) granted(R.string.settings_allowed) else refused(),
            onClick = actions::onNotifications,
        )
        Separator()
        SettingRow(
            icon = DesignR.drawable.ic_place,
            title = R.string.settings_location,
            text = R.string.settings_location_text,
            status = when (status.location) {
                LocationAccess.ALWAYS -> granted(R.string.settings_location_always)
                LocationAccess.WHILE_IN_USE -> off(R.string.settings_location_while_in_use)
                LocationAccess.DENIED -> off(R.string.settings_off)
            },
            onClick = actions::onLocation,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            SettingsContent(
                state = SettingsUiState(
                    permissions = PermissionStatus(
                        notificationAccess = true,
                        notificationsAllowed = false,
                        location = LocationAccess.WHILE_IN_USE,
                    ),
                    batteryUnrestricted = false,
                ),
                actions = PreviewSettingsActions,
            )
        }
    }
}

/** Actions that do nothing, for previews. */
private object PreviewSettingsActions : SettingsActions {
    override fun onBack() = Unit

    override fun onNotificationAccess() = Unit

    override fun onNotifications() = Unit

    override fun onLocation() = Unit

    override fun onBattery() = Unit
}
