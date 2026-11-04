package com.cauri.core.data.repository

import com.cauri.core.data.model.RestoreResult
import java.io.InputStream
import java.io.OutputStream

/** The encrypted backup of all the data of Cauri, to a file the user chooses, and its restore on a phone. */
interface BackupRepository {
    /** Writes the backup of the database and the preferences to [output], locked with [passphrase]. */
    suspend fun export(output: OutputStream, passphrase: CharArray)

    /**
     * Replaces the data of the phone by those of the backup read from [input] with [passphrase]. Once
     * [RestoreResult.RESTORED], the database is closed: the app must start again.
     */
    suspend fun restore(input: InputStream, passphrase: CharArray): RestoreResult
}
