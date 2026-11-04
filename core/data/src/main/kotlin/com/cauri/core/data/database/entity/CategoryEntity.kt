package com.cauri.core.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cauri.core.data.model.CategoryColor
import com.cauri.core.data.model.CategoryIcon
import com.cauri.core.data.model.CategoryNature

/**
 * A category of transactions, on two levels at most.
 *
 * @property id Generated identifier.
 * @property name Name shown to the user (« Transport »).
 * @property parentId Category this one belongs to; `null` for a top-level category.
 * @property nature Whether it counts spending, income or savings.
 * @property status Active, or archived by the user (its transactions keep it).
 * @property icon Icon chosen by the user; `null` for the one Cauri chooses from its name.
 * @property color Colour chosen by the user; `null` for the one Cauri chooses from its name.
 * @property position Place in the list the user ordered, from 0; equal ones by name.
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
    val icon: CategoryIcon? = null,
    val color: CategoryColor? = null,
    @ColumnInfo(defaultValue = "0") val position: Int = 0,
)
