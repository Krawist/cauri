package com.cauri.feature.settings

import com.cauri.core.capture.permission.PermissionStatus

/**
 * What the settings screen shows.
 *
 * @property permissions Status of each permission.
 * @property batteryUnrestricted Whether the phone lets Cauri run without battery restriction.
 */
internal data class SettingsUiState(val permissions: PermissionStatus, val batteryUnrestricted: Boolean)
