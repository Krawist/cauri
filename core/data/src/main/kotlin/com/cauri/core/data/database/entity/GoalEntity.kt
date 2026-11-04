package com.cauri.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cauri.core.data.model.GoalKind
import java.time.Instant
import java.time.LocalDate

/**
 * A goal of the user.
 *
 * @property id Generated identifier.
 * @property name What the user calls it.
 * @property kind Saving, tontine or envelope.
 * @property amount In minor units of XAF: the amount to put aside for a saving, the contribution a month for a tontine,
 *   the amount a month for an envelope.
 * @property deadline Day a saving should be reached by; `null` without deadline, and for the other kinds.
 * @property payoutDate Day the user takes the pot of a tontine; `null` when not known, and for the other kinds.
 * @property categoryId Category an envelope keeps money for; `null` for the other kinds.
 * @property fixedCostId Fixed cost of a tontine's contribution; `null` for the other kinds.
 * @property position Place in the list, from 0: the savings go to the first saving not reached.
 * @property createdAt When the user created it.
 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"]),
        ForeignKey(
            entity = FixedCostEntity::class,
            parentColumns = ["id"],
            childColumns = ["fixedCostId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("categoryId"), Index("fixedCostId")],
)
internal data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: GoalKind,
    val amount: Long,
    val deadline: LocalDate?,
    val payoutDate: LocalDate?,
    val categoryId: Long?,
    val fixedCostId: Long?,
    val position: Int,
    val createdAt: Instant,
)
