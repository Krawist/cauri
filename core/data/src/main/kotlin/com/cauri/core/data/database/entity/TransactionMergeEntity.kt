package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * The log of a merge: a manual entry and a detected transaction found to be the same operation. The detected one is
 * kept with the user's values; the manual one is [TransactionStatus.MERGED]. The values the kept transaction had
 * before are stored to undo the merge.
 *
 * @property id Generated identifier.
 * @property keptId Detected transaction kept, now holding the user's values.
 * @property mergedId Manual entry set aside.
 * @property mergedAt When the merge was made.
 * @property keptStatus Status of the kept transaction before the merge.
 * @property keptCategoryId Category of the kept transaction before the merge; `null` when it had none.
 * @property keptNote Note of the kept transaction before the merge; `null` when it had none.
 * @property keptPlaceId Place of the kept transaction before the merge; `null` when it had none.
 * @property undoneAt When the user undid the merge; `null` while it holds.
 */
@Entity(
    tableName = "transaction_merges",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["keptId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["mergedId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("keptId"), Index("mergedId")],
)
internal data class TransactionMergeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keptId: Long,
    val mergedId: Long,
    val mergedAt: Instant,
    val keptStatus: TransactionStatus,
    val keptCategoryId: Long?,
    val keptNote: String?,
    val keptPlaceId: Long?,
    val undoneAt: Instant?,
)
