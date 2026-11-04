package com.cauri.core.data.service

/** What reading a backup file gave. */
internal sealed interface OpenedBackup {
    /**
     * The backup, decrypted.
     *
     * @property files Its files by name: the database and the preferences.
     */
    data class Opened(val files: Map<String, ByteArray>) : OpenedBackup

    /** A backup of Cauri, but the passphrase is not the one it was made with, or the file was changed. */
    data object WrongPassphrase : OpenedBackup

    /** Not a backup of Cauri. */
    data object NotABackup : OpenedBackup
}
