package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * How the user split an income once it arrived, or that they chose not to.
 *
 * @property transactionId Income split, once.
 * @property savings Put aside, in minor units of XAF; 0 when not split.
 * @property fixedCosts Kept for the fixed costs of the month, in the same unit; 0 when not split.
 * @property budgets Left for the budgets and the spending, in the same unit; 0 when not split.
 * @property dismissed Whether the user chose not to split it.
 * @property splitAt When the user answered.
 */
@Entity(
    tableName = "income_splits",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class IncomeSplitEntity(
    @PrimaryKey val transactionId: Long,
    val savings: Long,
    val fixedCosts: Long,
    val budgets: Long,
    val dismissed: Boolean,
    val splitAt: Instant,
)
