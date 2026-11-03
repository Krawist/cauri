package com.cauri.core.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.model.CASH_PROVIDER

private const val CASH_ACCOUNT_CURRENCY = "XAF"

/**
 * Creates the Cash account with the database, and a trigger that refuses its deletion whatever the code that tries.
 * Triggers are not in Room's exported schema: a migration that recreates the accounts table must recreate it too.
 */
internal object CashAccountCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        connection.execSQL(
            "INSERT INTO accounts (id, name, kind, provider, currency, status) " +
                "VALUES ($CASH_ACCOUNT_ID, 'Cash', 'CASH', '$CASH_PROVIDER', '$CASH_ACCOUNT_CURRENCY', 'ACTIVE')",
        )
        connection.execSQL(KEEP_CASH_ACCOUNT)
    }
}

/** The trigger that refuses the deletion of the Cash account. */
internal const val KEEP_CASH_ACCOUNT =
    "CREATE TRIGGER keep_cash_account BEFORE DELETE ON accounts WHEN OLD.id = $CASH_ACCOUNT_ID " +
        "BEGIN SELECT RAISE(ABORT, 'The Cash account cannot be deleted'); END"

/** Builds the database with the Cash account. Production and tests both go through it. */
internal fun RoomDatabase.Builder<CauriDatabase>.buildCauriDatabase(): CauriDatabase =
    addCallback(CashAccountCallback).addMigrations(ProviderOfCashMigration).build()
