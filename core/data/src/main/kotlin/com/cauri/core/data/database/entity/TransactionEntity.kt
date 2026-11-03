package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A transaction on an account, parsed from a signal or entered by the user. A transfer is two transactions, a
 * [TransactionType.TRANSFER_OUT] and a [TransactionType.TRANSFER_IN], with the same [transferGroup].
 *
 * @property id Generated identifier.
 * @property accountId Account the money left or arrived on.
 * @property type Direction of the money.
 * @property status Detected, confirmed or dismissed.
 * @property origin How the transaction was created.
 * @property amount Amount in minor units of the account's currency, always positive (XAF has no decimals); 0 for an
 *   operation made in another currency, until the user types what it cost.
 * @property occurredAt When the money moved.
 * @property categoryId Category; `null` until one is chosen (and for transfers).
 * @property placeId Place where it happened; `null` when unknown.
 * @property rawSignalId Signal it was parsed from; `null` when not parsed. A signal creates at most one transaction.
 * @property transferGroup Identifier shared by the two sides of a transfer; `null` for other transactions.
 * @property counterparty Merchant or person on the other side, as the message names them; `null` when unknown.
 * @property note Free note of the user; `null` when there is none.
 * @property foreignAmount Amount in minor units of [foreignCurrency] for an operation made in another currency than the
 *   account's, such as a card payment in USD; `null` otherwise.
 * @property foreignCurrency ISO 4217 code of [foreignAmount]; `null` when the operation was in the account's currency.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"]),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"]),
        ForeignKey(entity = PlaceEntity::class, parentColumns = ["id"], childColumns = ["placeId"]),
        ForeignKey(entity = RawSignalEntity::class, parentColumns = ["id"], childColumns = ["rawSignalId"]),
    ],
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("placeId"),
        Index("rawSignalId", unique = true),
        Index("transferGroup"),
        Index("occurredAt"),
    ],
)
internal data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: TransactionType,
    val status: TransactionStatus,
    val origin: TransactionOrigin,
    val amount: Long,
    val occurredAt: Instant,
    val categoryId: Long?,
    val placeId: Long?,
    val rawSignalId: Long?,
    val transferGroup: String?,
    val counterparty: String?,
    val note: String?,
    val foreignAmount: Long?,
    val foreignCurrency: String?,
)
