package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.inMemoryDatabase
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RawSignalDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var rawSignalDao: RawSignalDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        rawSignalDao = database.rawSignalDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetUnparsed {
        @Test
        fun `should return the signals no template recognised, oldest first`() = runTest {
            // given
            val later =
                RawSignalEntity(
                    sender = "com.orange.om",
                    text = "Message 2",
                    receivedAt = Instant.ofEpochMilli(2_000),
                    templateVersion = null,
                )
            val earlier =
                RawSignalEntity(
                    sender = "com.mtn.momo",
                    text = "Message 1",
                    receivedAt = Instant.ofEpochMilli(1_000),
                    templateVersion = null,
                )
            val laterId = rawSignalDao.insert(later)
            val earlierId = rawSignalDao.insert(earlier)
            rawSignalDao.insert(
                RawSignalEntity(
                    sender = "com.mtn.momo",
                    text = "Paiement",
                    receivedAt = Instant.EPOCH,
                    templateVersion = "momo-1",
                ),
            )

            // when
            val unparsed = rawSignalDao.getUnparsed()

            // then
            unparsed shouldContainExactly listOf(earlier.copy(id = earlierId), later.copy(id = laterId))
        }
    }
}
