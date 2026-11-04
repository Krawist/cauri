package com.cauri.feature.settings.backup

/**
 * « Sauvegarde chiffrée ».
 *
 * @property restoreOnly Whether only the restore is offered, as from the onboarding of a new phone.
 * @property passphrase Passphrase typed.
 * @property confirmation Passphrase typed again, for a backup.
 * @property working Whether a backup or a restore is under way.
 * @property confirmingRestore Whether the dialog asks to replace the data of the phone.
 * @property message What to tell after the last action; `null` when nothing.
 * @property restored Whether the data were replaced: the app starts again.
 */
internal data class BackupUiState(
    val restoreOnly: Boolean = false,
    val passphrase: String = "",
    val confirmation: String = "",
    val working: Boolean = false,
    val confirmingRestore: Boolean = false,
    val message: BackupMessage? = null,
    val restored: Boolean = false,
)
