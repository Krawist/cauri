package com.cauri.core.data.repository

import com.cauri.core.data.database.dao.RawSignalDao
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.model.RawSignal
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [RawSignalRepository]. */
@Singleton
internal class RawSignalRepositoryImpl @Inject constructor(
    private val rawSignalDao: RawSignalDao,
    private val clock: Clock,
) : RawSignalRepository {
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

    override suspend fun lastReceivedAt(): Instant? = rawSignalDao.getLastReceivedAt()

    override suspend fun unparsed(): List<RawSignal> = rawSignalDao.getUnparsed().map { it.toModel() }

    override suspend fun unreadByModel(since: Instant, limit: Int): List<RawSignal> =
        rawSignalDao.getUnreadByModel(since = since, limit = limit).map { it.toModel() }

    override suspend fun markReadByModel(id: Long) = rawSignalDao.setModelReadAt(id = id, at = clock.instant())

    private fun RawSignalEntity.toModel() =
        RawSignal(id = id, sender = sender, title = title, text = text, receivedAt = receivedAt)

    private companion object {
        // A notification posted again within this time, with the same text, is the same one.
        val REPOST_WINDOW: Duration = Duration.ofMinutes(10)
    }
}
