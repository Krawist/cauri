package com.cauri.core.data.database.dao

import androidx.room.Embedded
import com.cauri.core.data.database.entity.PlaceEntity

/**
 * A place with the number of visits detected there.
 *
 * @property place The place.
 * @property visits How many visits were detected there.
 */
internal data class PlaceVisits(@Embedded val place: PlaceEntity, val visits: Int)
