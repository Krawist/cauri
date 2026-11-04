package com.cauri.core.data.repository

import com.cauri.core.data.model.RestoreResult
import java.io.InputStream
import java.io.OutputStream

/** In-memory [BackupRepository] for tests: the test sets what a restore answers; the passphrases used are kept. */
class FakeBackupRepository : BackupRepository {
    var restoreResult = RestoreResult.RESTORED

    val exported = mutableListOf<String>()

    val restored = mutableListOf<String>()

    override suspend fun export(output: OutputStream, passphrase: CharArray) {
        exported += String(passphrase)
    }

    override suspend fun restore(input: InputStream, passphrase: CharArray): RestoreResult {
        restored += String(passphrase)
        return restoreResult
    }
}
