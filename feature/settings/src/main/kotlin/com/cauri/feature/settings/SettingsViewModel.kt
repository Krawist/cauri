package com.cauri.feature.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.permission.PermissionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Settings screen: the status of each permission, changed in the system settings. The screen calls [refresh] when the
 * user comes back from them.
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(private val permissions: PermissionService) : ViewModel() {
    private val state = MutableStateFlow(permissions.status())

    val status: StateFlow<PermissionStatus> = state.asStateFlow()

    fun refresh() {
        state.value = permissions.status()
    }

    /** System screen where the user gives or takes back the access to the notifications. */
    fun notificationAccessSettings(): Intent = permissions.notificationAccessSettings()

    /** System screen of Cauri's own notifications. */
    fun notificationSettings(): Intent = permissions.notificationSettings()

    /** System screen of Cauri's permissions, where the location is changed. */
    fun locationSettings(): Intent = permissions.appSettings()
}
