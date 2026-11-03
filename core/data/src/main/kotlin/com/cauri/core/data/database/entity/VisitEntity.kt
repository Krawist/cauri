package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A stay detected at a place.
 *
 * @property id Generated identifier.
 * @property placeId Place of the stay.
 * @property enteredAt When the user entered the place.
 * @property leftAt When the user left it; `null` while they are still there.
 * @property transactionId Cash expense to confirm the visit created, the user having left with nothing paid there;
 *   `null` when it created none.
 */
@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = PlaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["placeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("placeId")],
)
internal data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val placeId: Long,
    val enteredAt: Instant,
    val leftAt: Instant?,
    val transactionId: Long? = null,
)
