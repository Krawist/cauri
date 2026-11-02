package com.cauri.feature.onboarding.permissions

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
 * Permissions steps of the onboarding: what the user allowed so far. Android asks the questions itself, so the screens
 * call [refresh] after each answer and when the user comes back from the system settings.
 */
@HiltViewModel
internal class PermissionsViewModel @Inject constructor(private val permissions: PermissionService) : ViewModel() {
    private val state = MutableStateFlow(permissions.status())

    val status: StateFlow<PermissionStatus> = state.asStateFlow()

    fun refresh() {
        state.value = permissions.status()
    }

    /** System screen where the user gives Cauri access to the notifications. */
    fun notificationAccessSettings(): Intent = permissions.notificationAccessSettings()
}
