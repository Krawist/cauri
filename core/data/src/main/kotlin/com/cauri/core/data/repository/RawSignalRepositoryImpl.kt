package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.RawSignalDao
import com.cauri.core.data.database.entity.RawSignalEntity
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [RawSignalRepository]. */
@Singleton
internal class RawSignalRepositoryImpl @Inject constructor(private val rawSignalDao: RawSignalDao) :
    RawSignalRepository {
    override suspend fun record(sender: String, title: String?, text: String, receivedAt: Instant): Boolean {
        val repost = rawSignalDao.existsSince(
            sender = sender,
            title = title,
            text = text,
            after = receivedAt - REPOST_WINDOW,
        )
        if (!repost) {
            rawSignalDao.insert(
                RawSignalEntity(
                    sender = sender,
                    title = title,
                    text = text,
                    receivedAt = receivedAt,
                    templateVersion = null,
                ),
            )
        }
        return !repost
    }

    private companion object {
        // A notification posted again within this time, with the same text, is the same one.
        val REPOST_WINDOW: Duration = Duration.ofMinutes(10)
    }
}
