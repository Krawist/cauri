package com.cauri.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.cauri.core.data.model.CASH_PROVIDER

/**
 * Version 5: every account has a provider, [CASH_PROVIDER] for the Cash account. SQLite cannot make a column
 * NOT NULL in place: the accounts table is recreated, then the trigger that keeps the Cash account, dropped with it.
 */
internal object ProviderOfCashMigration : Migration(startVersion = 4, endVersion = 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("UPDATE accounts SET provider = '$CASH_PROVIDER' WHERE provider IS NULL")
        connection.execSQL(
            "CREATE TABLE _new_accounts (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`kind` TEXT NOT NULL, `provider` TEXT NOT NULL, `currency` TEXT NOT NULL, `status` TEXT NOT NULL)",
        )
        connection.execSQL(
            "INSERT INTO _new_accounts (id, name, kind, provider, currency, status) " +
                "SELECT id, name, kind, provider, currency, status FROM accounts",
        )
        connection.execSQL("DROP TABLE accounts")
        connection.execSQL("ALTER TABLE _new_accounts RENAME TO accounts")
        connection.execSQL(KEEP_CASH_ACCOUNT)
    }
}
