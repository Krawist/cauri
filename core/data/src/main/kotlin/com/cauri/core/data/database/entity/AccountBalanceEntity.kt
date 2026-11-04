package com.cauri.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A balance of an account, as the operator's message gave it after an operation or as the user typed it. The latest
 * one, by [at], read or typed, is the balance shown for the account.
 *
 * @property id Generated identifier.
 * @property accountId Account it is the balance of.
 * @property amount Balance in minor units of the account's currency; negative for an overdrawn bank account.
 * @property at When the account had this balance: the time of the operation.
 * @property rawSignalId Signal the balance was read in, `null` for a balance the user typed or Cauri estimated. A
 *   signal gives at most one balance.
 * @property estimated Whether Cauri estimated it: the balance before moved by a transfer whose message on this account
 *   never came (Android hides some), replaced by the balance that message gives.
 */
@Entity(
    tableName = "account_balances",
    foreignKeys = [
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"]),
        ForeignKey(entity = RawSignalEntity::class, parentColumns = ["id"], childColumns = ["rawSignalId"]),
    ],
    indices = [Index("accountId"), Index("rawSignalId", unique = true)],
)
internal data class AccountBalanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val amount: Long,
    val at: Instant,
    val rawSignalId: Long?,
    @ColumnInfo(defaultValue = "0") val estimated: Boolean = false,
)
