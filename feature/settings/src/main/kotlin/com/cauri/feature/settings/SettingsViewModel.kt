package com.cauri.feature.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.cauri.core.capture.battery.BatteryService
import com.cauri.core.capture.permission.PermissionService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Settings screen: the status of each permission and of the battery, changed in the system settings. The screen calls
 * [refresh] when the user comes back from them.
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val permissions: PermissionService,
    private val battery: BatteryService,
) : ViewModel() {
    private val state = MutableStateFlow(read())

    val uiState: StateFlow<SettingsUiState> = state.asStateFlow()

    fun refresh() {
        state.value = read()
    }

    /** System screen where the user gives or takes back the access to the notifications. */
    fun notificationAccessSettings(): Intent = permissions.notificationAccessSettings()

    /** System screen of Cauri's own notifications. */
    fun notificationSettings(): Intent = permissions.notificationSettings()

    /** System screen of Cauri's permissions, where the location is changed. */
    fun locationSettings(): Intent = permissions.appSettings()

    /** System screen of Cauri's details, whose « Batterie » entry lifts its restriction. */
    fun batterySettings(): Intent = battery.appSettings()

    private fun read() =
        SettingsUiState(permissions = permissions.status(), batteryUnrestricted = battery.unrestricted())
}
