package com.cauri.feature.onboarding.security

/**
 * The step « Protège ton argent ».
 *
 * @property available Whether the phone has a screen lock, which the lock of Cauri needs.
 * @property enabled Whether the lock of Cauri is on.
 */
internal data class SecurityUiState(val available: Boolean = false, val enabled: Boolean = false)
