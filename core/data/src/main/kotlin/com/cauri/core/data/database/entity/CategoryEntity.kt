package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cauri.core.data.model.CategoryNature

/**
 * A category of transactions, on two levels at most.
 *
 * @property id Generated identifier.
 * @property name Name shown to the user (« Transport »).
 * @property parentId Category this one belongs to; `null` for a top-level category.
 * @property nature Whether it counts spending, income or savings.
 * @property status Active, or archived by the user (its transactions keep it).
 */
@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["parentId"])],
    indices = [Index("parentId")],
)
internal data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long?,
    val nature: CategoryNature,
    val status: CategoryStatus,
)
