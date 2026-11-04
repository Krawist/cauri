package com.cauri.core.data.repository

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.model.RestoreResult
import com.cauri.core.data.service.BackupService
import com.cauri.core.data.service.OpenedBackup
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Default implementation for [BackupRepository]. */
@Singleton
internal class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CauriDatabase,
    private val service: BackupService,
) : BackupRepository {
    private val databaseFile: File get() = context.getDatabasePath(DATABASE)
    private val preferencesFile: File get() = context.preferencesDataStoreFile(PREFERENCES)

    override suspend fun export(output: OutputStream, passphrase: CharArray) = withContext(Dispatchers.IO) {
        // Everything written so far moves from the write-ahead log into the database file.
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        val files = buildMap {
            put(DATABASE, databaseFile.readBytes())
            if (preferencesFile.exists()) put(PREFERENCES, preferencesFile.readBytes())
        }
        output.use { it.write(service.write(files, passphrase)) }
    }

    override suspend fun restore(input: InputStream, passphrase: CharArray): RestoreResult =
        withContext(Dispatchers.IO) {
            val opened = service.read(input.use { it.readBytes() }, passphrase)
            val files = (opened as? OpenedBackup.Opened)?.files
            val backup = files?.get(DATABASE)
            when {
                opened == OpenedBackup.WrongPassphrase -> RestoreResult.WRONG_PASSPHRASE
                backup == null || backup.size < HEADER_SIZE -> RestoreResult.NOT_A_BACKUP
                version(backup) > database.openHelper.readableDatabase.version -> RestoreResult.TOO_NEW
                else -> {
                    database.close()
                    listOf("-wal", "-shm").forEach { File(databaseFile.path + it).delete() }
                    databaseFile.writeBytes(backup)
                    files[PREFERENCES]?.let { preferencesFile.writeBytes(it) }
                    RestoreResult.RESTORED
                }
            }
        }

    // The schema version Room wrote in the SQLite header (« user_version », four bytes at offset 60); an older one is
    // migrated when the app starts again.
    private fun version(database: ByteArray) = ByteBuffer.wrap(database, VERSION_OFFSET, Int.SIZE_BYTES).int

    private companion object {
        const val DATABASE = "cauri.db"
        const val PREFERENCES = "user_preferences"
        const val VERSION_OFFSET = 60
        const val HEADER_SIZE = 100
    }
}
