package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A habitual cash expense, recorded in one tap from the home screen (« Moto 500 F »).
 *
 * @property id Generated identifier.
 * @property name Name shown on the tile and given to the expenses it records.
 * @property amount Amount of each expense, in minor units of the Cash account's currency.
 * @property categoryId Category of the expenses it records; `null` when none was chosen, or once it is deleted.
 * @property position Place of the habit in the row, from 0.
 */
@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("categoryId")],
)
internal data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long,
    val categoryId: Long?,
    val position: Int,
)
