package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.BanditModelDao
import com.cauri.core.data.database.entity.BanditModelEntity
import com.cauri.core.data.model.BanditSnapshot
import com.cauri.core.data.model.NewBanditSnapshot
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [BanditRepository]. */
@Singleton
internal class BanditRepositoryImpl @Inject constructor(private val banditModelDao: BanditModelDao) :
    BanditRepository {
    override suspend fun latest(): BanditSnapshot? = banditModelDao.getLatest()?.let {
        BanditSnapshot(
            version = it.version,
            trainedAt = it.trainedAt,
            featureVersion = it.featureVersion,
            rewardVersion = it.rewardVersion,
            lastDecisionId = it.lastDecisionId,
            decisions = it.decisions,
            parameters = it.parameters,
        )
    }

    override suspend fun publish(model: NewBanditSnapshot): Long = banditModelDao.insert(
        BanditModelEntity(
            trainedAt = model.trainedAt,
            featureVersion = model.featureVersion,
            rewardVersion = model.rewardVersion,
            lastDecisionId = model.lastDecisionId,
            decisions = model.decisions,
            parameters = model.parameters,
        ),
    )

    override suspend fun deleteAll() = banditModelDao.deleteAll()
}
