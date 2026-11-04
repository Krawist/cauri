package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Money put aside for a saving, or taken back from it.
 *
 * @property id Generated identifier.
 * @property goalId The saving.
 * @property amount In minor units of XAF: positive when put aside, negative when taken back.
 * @property at When.
 * @property transactionId Income whose split put it aside; `null` when the user did it by hand.
 */
@Entity(
    tableName = "goal_contributions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("goalId"), Index("transactionId")],
)
internal data class GoalContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Long,
    val at: Instant,
    val transactionId: Long?,
)
