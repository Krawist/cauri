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
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.NewAccount
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/** Settings: each permission and the battery with their status; a tap opens the system screen where it is changed. */
@Composable
internal fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPlaces: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenIncome: () -> Unit,
    onOpenBackup: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    // Named apart from the action it backs, which shares its name.
    val back = onBack
    val actions = remember(viewModel, context, back, onOpenPlaces, onOpenReminders, onOpenIncome, onOpenBackup) {
        object : SettingsActions {
            override fun onBack() = back()

            override fun onNotificationAccess() = context.startActivity(viewModel.notificationAccessSettings())

            override fun onNotifications() = context.startActivity(viewModel.notificationSettings())

            override fun onLocation() = context.startActivity(viewModel.locationSettings())

            override fun onBattery() = context.startActivity(viewModel.batterySettings())

            override fun onPlaces() = onOpenPlaces()

            override fun onSmartReminders() = onOpenReminders()

            override fun onIncome() = onOpenIncome()

            override fun onBackup() = onOpenBackup()

            override fun onCountReminder(open: Boolean) = viewModel.onChooseReminder(open)

            override fun onCountReminderChange(settings: CountReminderSettings) = viewModel.onReminderChange(settings)

            override fun onFollowAccount(provider: String) = viewModel.onFollowAccount(provider)
        }
    }
    SettingsContent(state = state, actions = actions)
    if (state.choosingReminder) CountReminderSheet(settings = state.countReminder, actions = actions)
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
        MoneyGroup(actions = actions)
        SectionLabel(
            stringResource(R.string.settings_permissions),
            modifier = Modifier.padding(top = CauriSpacing.xxl),
        )
        PermissionsGroup(status = state.permissions, actions = actions)
        PlacesGroup(actions = actions)
        SectionLabel(
            stringResource(R.string.settings_battery_section),
            modifier = Modifier.padding(top = CauriSpacing.xxl),
        )
        RowGroup {
            SettingRow(
                icon = DesignR.drawable.ic_battery,
                title = stringResource(R.string.settings_battery),
                text = stringResource(R.string.settings_battery_text),
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
        SectionLabel(stringResource(R.string.settings_reminders), modifier = Modifier.padding(top = CauriSpacing.xxl))
        RemindersGroup(settings = state.countReminder, actions = actions)
        if (state.ignoredAccounts.isNotEmpty()) {
            SectionLabel(
                stringResource(R.string.settings_ignored_accounts),
                modifier = Modifier.padding(top = CauriSpacing.xxl),
            )
            IgnoredAccountsGroup(accounts = state.ignoredAccounts, actions = actions)
        }
        DataGroup(actions = actions)
    }
}

// « Sauvegarde chiffrée », last.
@Composable
private fun DataGroup(actions: SettingsActions) {
    SectionLabel(stringResource(R.string.settings_data_section), modifier = Modifier.padding(top = CauriSpacing.xxl))
    RowGroup {
        SettingRow(
            icon = DesignR.drawable.ic_check,
            title = stringResource(R.string.settings_backup),
            text = stringResource(R.string.settings_backup_text),
            status = null,
            onClick = actions::onBackup,
        )
    }
}

// « Répartir mes revenus », first: the money before the permissions.
@Composable
private fun MoneyGroup(actions: SettingsActions) {
    SectionLabel(stringResource(R.string.settings_money_section))
    RowGroup {
        SettingRow(
            icon = DesignR.drawable.ic_income,
            title = stringResource(R.string.settings_income),
            text = stringResource(R.string.settings_income_text),
            status = null,
            onClick = actions::onIncome,
        )
    }
}

// « Mes lieux », under the location it relies on.
@Composable
private fun PlacesGroup(actions: SettingsActions) {
    SectionLabel(stringResource(R.string.settings_places_section), modifier = Modifier.padding(top = CauriSpacing.xxl))
    RowGroup {
        SettingRow(
            icon = DesignR.drawable.ic_place,
            title = stringResource(R.string.settings_places),
            text = stringResource(R.string.settings_places_text),
            status = null,
            onClick = actions::onPlaces,
        )
    }
}

// A tap follows the account: Cauri reads its messages again, from the first one it kept.
@Composable
private fun IgnoredAccountsGroup(accounts: List<NewAccount>, actions: SettingsActions) {
    RowGroup {
        accounts.forEachIndexed { index, account ->
            if (index > 0) Separator()
            SettingRow(
                icon = DesignR.drawable.ic_message,
                title = account.name,
                text = stringResource(R.string.settings_ignored_account_text),
                status = off(R.string.settings_ignored),
                onClick = { actions.onFollowAccount(account.provider) },
            )
        }
    }
}

// « Rappels intelligents », then the cash count reminders.
@Composable
private fun RemindersGroup(settings: CountReminderSettings, actions: SettingsActions) {
    RowGroup {
        SettingRow(
            icon = DesignR.drawable.ic_bell,
            title = stringResource(R.string.settings_smart_reminders),
            text = stringResource(R.string.settings_smart_reminders_text),
            status = null,
            onClick = actions::onSmartReminders,
        )
        Separator()
        SettingRow(
            icon = DesignR.drawable.ic_clock,
            title = stringResource(R.string.settings_count_reminder),
            text = if (settings.frequency == CountReminderFrequency.NEVER) {
                stringResource(R.string.settings_count_reminder_none)
            } else {
                stringResource(R.string.settings_count_reminder_from, settings.hour)
            },
            status = when (settings.frequency) {
                CountReminderFrequency.WHEN_USEFUL -> active(R.string.settings_count_reminder_when_useful_short)
                CountReminderFrequency.EVERY_EVENING -> active(R.string.settings_count_reminder_every_evening_short)
                CountReminderFrequency.NEVER -> off(R.string.settings_off)
            },
            onClick = { actions.onCountReminder(open = true) },
        )
    }
}

@Composable
private fun PermissionsGroup(status: PermissionStatus, actions: SettingsActions) {
    RowGroup {
        SettingRow(
            icon = DesignR.drawable.ic_message,
            title = stringResource(R.string.settings_notification_access),
            text = stringResource(R.string.settings_notification_access_text),
            status = if (status.notificationAccess) granted(R.string.settings_allowed) else refused(),
            onClick = actions::onNotificationAccess,
        )
        Separator()
        SettingRow(
            icon = DesignR.drawable.ic_bell,
            title = stringResource(R.string.settings_notifications),
            text = stringResource(R.string.settings_notifications_text),
            status = if (status.notificationsAllowed) granted(R.string.settings_allowed) else refused(),
            onClick = actions::onNotifications,
        )
        Separator()
        SettingRow(
            icon = DesignR.drawable.ic_place,
            title = stringResource(R.string.settings_location),
            text = stringResource(R.string.settings_location_text),
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
// One function per row of the screen.
@Suppress("TooManyFunctions")
private object PreviewSettingsActions : SettingsActions {
    override fun onBack() = Unit

    override fun onNotificationAccess() = Unit

    override fun onNotifications() = Unit

    override fun onLocation() = Unit

    override fun onBattery() = Unit

    override fun onPlaces() = Unit

    override fun onSmartReminders() = Unit

    override fun onIncome() = Unit

    override fun onBackup() = Unit

    override fun onCountReminder(open: Boolean) = Unit

    override fun onCountReminderChange(settings: CountReminderSettings) = Unit

    override fun onFollowAccount(provider: String) = Unit
}
