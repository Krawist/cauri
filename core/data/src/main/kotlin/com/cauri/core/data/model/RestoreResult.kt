package com.cauri.core.data.model

/** What restoring a backup gave. */
enum class RestoreResult {
    /** The data of the backup replaced those of the phone: the app must start again to read them. */
    RESTORED,

    /** The passphrase is not the one the backup was made with, or the file was changed. */
    WRONG_PASSPHRASE,

    /** The file is no backup of Cauri. */
    NOT_A_BACKUP,

    /** The backup comes from a newer Cauri: it is to be updated first. */
    TOO_NEW,
}
