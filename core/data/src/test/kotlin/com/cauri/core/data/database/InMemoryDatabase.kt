package com.cauri.core.data.database

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/** A database in memory with the bundled SQLite driver, built like the real one ([buildCauriDatabase]). */
internal fun inMemoryDatabase(): CauriDatabase =
    // An in-memory database with a driver never reads the context: unit tests run without Android.
    Room.inMemoryDatabaseBuilder(ContextWrapper(null), CauriDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .buildCauriDatabase()
