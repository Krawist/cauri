package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * The monthly budget of a category, or the suggestion of one the user ignored.
 *
 * @property categoryId Category budgeted, one budget each.
 * @property amount Amount a month, in minor units of XAF; 0 when ignored.
 * @property status Whether it holds or was ignored.
 * @property suggestedAmount Amount Cauri suggested, in the same unit, next to the one kept; `null` when typed alone.
 * @property updatedAt When it was last set.
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class BudgetEntity(
    @PrimaryKey val categoryId: Long,
    val amount: Long,
    val status: BudgetStatus,
    val suggestedAmount: Long?,
    val updatedAt: Instant,
)
