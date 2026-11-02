package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A count of the cash in pocket. The latest one is the starting point of the estimated Cash balance.
 *
 * @property id Generated identifier.
 * @property countedAt When the user counted.
 * @property amount Amount counted, in minor units of the Cash account's currency.
 * @property expectedAmount Estimated balance at that moment, in the same unit; `null` for the first count.
 */
@Entity(tableName = "cash_counts", indices = [Index("countedAt")])
internal data class CashCountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val countedAt: Instant,
    val amount: Long,
    val expectedAmount: Long?,
)
