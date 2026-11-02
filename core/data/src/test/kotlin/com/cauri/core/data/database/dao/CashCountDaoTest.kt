package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.inMemoryDatabase
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CashCountDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var cashCountDao: CashCountDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        cashCountDao = database.cashCountDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetLatest {
        @Test
        fun `should return null before the first count`() = runTest {
            // when
            val latest = cashCountDao.getLatest()

            // then
            latest.shouldBeNull()
        }

        @Test
        fun `should return the most recent count`() = runTest {
            // given
            val recent =
                CashCountEntity(countedAt = Instant.ofEpochMilli(2_000), amount = 7_500, expectedAmount = 12_000)
            val recentId = cashCountDao.insert(recent)
            cashCountDao.insert(
                CashCountEntity(countedAt = Instant.ofEpochMilli(1_000), amount = 15_000, expectedAmount = null),
            )

            // when
            val latest = cashCountDao.getLatest()

            // then
            latest shouldBe recent.copy(id = recentId)
        }
    }
}
