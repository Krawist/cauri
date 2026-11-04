package com.cauri.core.data.service

/**
 * The format of the backup files: the files zipped, then encrypted with AES-256-GCM under a key derived from the
 * user's passphrase (PBKDF2 with HMAC-SHA256, a random salt each time). Nothing of the content is readable without the
 * passphrase, and a file changed by a single byte does not open.
 */
internal interface BackupService {
    /** The backup file of [files], by name, locked with [passphrase]. */
    fun write(files: Map<String, ByteArray>, passphrase: CharArray): ByteArray

    /** The files of the backup [file], opened with [passphrase]. */
    fun read(file: ByteArray, passphrase: CharArray): OpenedBackup
}
