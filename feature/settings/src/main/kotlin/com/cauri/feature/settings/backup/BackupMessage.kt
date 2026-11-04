package com.cauri.feature.settings.backup

/** What « Sauvegarde chiffrée » tells after the user acted. */
internal enum class BackupMessage {
    /** The backup file was written. */
    SAVED,

    /** The passphrase has fewer than 8 characters. */
    TOO_SHORT,

    /** The two passphrases typed differ. */
    MISMATCH,

    /** The passphrase does not open the file chosen. */
    WRONG_PASSPHRASE,

    /** The file chosen is no backup of Cauri. */
    NOT_A_BACKUP,

    /** The backup comes from a newer Cauri. */
    TOO_NEW,

    /** The file could not be read or written. */
    FAILED,
}
