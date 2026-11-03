package com.cauri.core.data.database.dao

import androidx.room.Embedded
import com.cauri.core.data.database.entity.VisitEntity

/**
 * A visit with what the card of the visit shows of its place.
 *
 * @property visit The visit.
 * @property placeName Name of the place.
 * @property categoryId Default category of the place; `null` when it has none.
 * @property proposedAmount Usual amount the card of the visit proposes, in minor units; `null` when there is no card
 *   yet or no usual amount.
 */
internal data class VisitAtPlace(
    @Embedded val visit: VisitEntity,
    val placeName: String,
    val categoryId: Long?,
    val proposedAmount: Long?,
)
