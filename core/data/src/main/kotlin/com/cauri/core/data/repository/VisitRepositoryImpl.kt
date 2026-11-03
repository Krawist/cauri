package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.VisitDao
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [VisitRepository]. */
@Singleton
internal class VisitRepositoryImpl @Inject constructor(private val visitDao: VisitDao) : VisitRepository {
    override suspend fun enter(placeId: Long, at: Instant) = visitDao.enter(placeId = placeId, enteredAt = at)

    override suspend fun leave(placeId: Long, at: Instant) = visitDao.leave(placeId = placeId, leftAt = at)
}
