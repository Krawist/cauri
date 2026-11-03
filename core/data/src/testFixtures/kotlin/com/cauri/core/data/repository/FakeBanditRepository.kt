package com.cauri.core.data.repository

import com.cauri.core.data.model.BanditSnapshot
import com.cauri.core.data.model.NewBanditSnapshot

/** In-memory [BanditRepository] for tests: it keeps every model published, the latest last. */
class FakeBanditRepository : BanditRepository {
    val models = mutableListOf<BanditSnapshot>()

    override suspend fun latest() = models.lastOrNull()

    override suspend fun publish(model: NewBanditSnapshot): Long {
        val version = (models.lastOrNull()?.version ?: 0) + 1
        models += BanditSnapshot(
            version = version,
            trainedAt = model.trainedAt,
            featureVersion = model.featureVersion,
            rewardVersion = model.rewardVersion,
            lastDecisionId = model.lastDecisionId,
            decisions = model.decisions,
            parameters = model.parameters,
        )
        return version
    }

    override suspend fun deleteAll() = models.clear()
}
