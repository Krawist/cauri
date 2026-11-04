package com.cauri

/**
 * Whether Cauri is locked by the phone's security.
 *
 * @property enabled Whether the user turned the lock on; `null` until the setting is read.
 * @property locked Whether the app is hidden behind the lock screen until the user is recognised.
 */
internal data class AppLockUiState(val enabled: Boolean? = null, val locked: Boolean = false)
