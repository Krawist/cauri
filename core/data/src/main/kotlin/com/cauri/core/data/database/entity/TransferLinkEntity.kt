package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * The log of a link: a debit on one account and a credit on another found to be one transfer between the user's
 * accounts, such as UBA to Orange Money. Both become the two sides of the transfer; when less arrived than left, the
 * gap is a fee on the debited account. The values before are stored to undo the link.
 *
 * @property id Generated identifier.
 * @property debitId Expense that became the outgoing side.
 * @property creditId Income that became the incoming side.
 * @property feeId Fee created for the gap between the two amounts; `null` when both amounts were equal.
 * @property debitAmount Amount of the debit before the link, in minor units: the fee took its part of it.
 * @property linkedAt When the link was made.
 * @property undoneAt When the user undid the link; `null` while it holds. An undone link is never made again.
 */
@Entity(
    tableName = "transfer_links",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["debitId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("debitId"), Index("creditId")],
)
internal data class TransferLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debitId: Long,
    val creditId: Long,
    val feeId: Long?,
    val debitAmount: Long,
    val linkedAt: Instant,
    val undoneAt: Instant?,
)
