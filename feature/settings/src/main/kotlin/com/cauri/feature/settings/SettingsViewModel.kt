package com.cauri.feature.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings screen: the status of each permission and of the battery, changed in the system settings, and the cash
 * count reminders. The screen calls [refresh] when the user comes back from the system settings.
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val permissions: PermissionService,
    private val battery: BatteryService,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    private val state = MutableStateFlow(
        SettingsUiState(permissions = permissions.status(), batteryUnrestricted = battery.unrestricted()),
    )

    val uiState: StateFlow<SettingsUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.countReminder.collect { settings -> state.update { it.copy(countReminder = settings) } }
        }
    }

    fun refresh() = state.update {
        it.copy(permissions = permissions.status(), batteryUnrestricted = battery.unrestricted())
    }

    /** Opens or closes the sheet of the cash count reminders. */
    fun onChooseReminder(open: Boolean) = state.update { it.copy(choosingReminder = open) }

    fun onReminderChange(settings: CountReminderSettings) {
        viewModelScope.launch { preferences.saveCountReminder(settings) }
    }

    /** System screen where the user gives or takes back the access to the notifications. */
    fun notificationAccessSettings(): Intent = permissions.notificationAccessSettings()

    /** System screen of Cauri's own notifications. */
    fun notificationSettings(): Intent = permissions.notificationSettings()

    /** System screen of Cauri's permissions, where the location is changed. */
    fun locationSettings(): Intent = permissions.appSettings()

    /** System screen of Cauri's details, whose « Batterie » entry lifts its restriction. */
    fun batterySettings(): Intent = battery.appSettings()
}
