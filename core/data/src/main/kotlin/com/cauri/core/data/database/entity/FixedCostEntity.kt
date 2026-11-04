package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A cost that comes back every month, such as the rent or the electricity, or a suggested one the user refused.
 *
 * @property id Generated identifier.
 * @property name What the user calls it, or the merchant it was found from.
 * @property amount Amount a month, in minor units of XAF.
 * @property dayOfMonth Day of the month it is usually paid, from 1 to 31.
 * @property categoryId Category of its payments; `null` when not known.
 * @property counterparty Merchant its payments name in the messages, as written; `null` when none does.
 * @property status Whether it holds or was refused.
 * @property createdAt When it was typed, confirmed or refused.
 */
@Entity(
    tableName = "fixed_costs",
    foreignKeys = [ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"])],
    indices = [Index("categoryId")],
)
internal data class FixedCostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long,
    val dayOfMonth: Int,
    val categoryId: Long?,
    val counterparty: String?,
    val status: FixedCostStatus,
    val createdAt: Instant,
)
