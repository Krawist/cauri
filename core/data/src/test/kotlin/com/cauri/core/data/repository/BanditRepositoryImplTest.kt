package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.NewBanditSnapshot
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BanditRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: BanditRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = BanditRepositoryImpl(database.banditModelDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Publish {
        @Test
        fun `should publish each model as the next version, the latest deciding`() = runTest {
            // given
            repository.publish(model(lastDecisionId = 10))

            // when
            val version = repository.publish(model(lastDecisionId = 20))

            // then
            version shouldBe 2
            repository.latest().shouldNotBeNull().let { it.version to it.lastDecisionId } shouldBe (2L to 20L)
        }
    }

    @Nested
    inner class DeleteAll {
        @Test
        fun `should forget every model`() = runTest {
            // given
            repository.publish(model(lastDecisionId = 10))

            // when
            repository.deleteAll()

            // then
            repository.latest().shouldBeNull()
        }
    }

    private fun model(lastDecisionId: Long) = NewBanditSnapshot(
        trainedAt = Instant.EPOCH,
        featureVersion = 1,
        rewardVersion = "rewards-1",
        lastDecisionId = lastDecisionId,
        decisions = 150,
        parameters = listOf(1.0, 2.5),
    )
}
