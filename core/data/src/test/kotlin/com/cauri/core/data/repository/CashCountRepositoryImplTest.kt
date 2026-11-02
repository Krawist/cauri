package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.CashCountEntity
import com.cauri.core.data.database.inMemoryDatabase
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CashCountRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: CashCountRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = CashCountRepositoryImpl(database.cashCountDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class AddCount {
        @Test
        fun `should save the count`() = runTest {
            // when
            repository.addCount(amount = 7_500, countedAt = Instant.ofEpochMilli(1_000), expectedAmount = null)

            // then
            database.cashCountDao().getLatest().shouldNotBeNull().shouldBeEqualToIgnoringFields(
                CashCountEntity(countedAt = Instant.ofEpochMilli(1_000), amount = 7_500, expectedAmount = null),
                CashCountEntity::id,
            )
        }
    }
}
