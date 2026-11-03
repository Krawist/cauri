package com.cauri.core.data.database.dao

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.DecisionLogEntity
import com.cauri.core.data.database.entity.DecisionOutcome
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.ReminderAction
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DecisionLogDaoTest {
    private lateinit var database: CauriDatabase
    private lateinit var decisionLogDao: DecisionLogDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        decisionLogDao = database.decisionLogDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetSince {
        @Test
        fun `should return the decisions of the window with their features unchanged, oldest first`() = runTest {
            // given
            val first = decision(at = 2_000, features = listOf(1.0, 0.25, -3.5), outcome = DecisionOutcome.CONFIRMED)
            val second = decision(at = 3_000, features = emptyList(), outcome = null)
            decisionLogDao.insert(decision(at = 1_000, features = listOf(0.0), outcome = DecisionOutcome.TIMEOUT))
            val secondId = decisionLogDao.insert(second)
            val firstId = decisionLogDao.insert(first)

            // when
            val window = decisionLogDao.getSince(Instant.ofEpochMilli(2_000))

            // then
            window shouldContainExactly listOf(first.copy(id = firstId), second.copy(id = secondId))
        }
    }

    @Nested
    inner class GetOpen {
        @Test
        fun `should return the decisions with no outcome yet`() = runTest {
            // given
            val open = decision(at = 2_000, features = listOf(1.0), outcome = null)
            val openId = decisionLogDao.insert(open)
            decisionLogDao.insert(decision(at = 1_000, features = listOf(1.0), outcome = DecisionOutcome.NOTHING_SPENT))

            // when
            val decisions = decisionLogDao.getOpen()

            // then
            decisions shouldContainExactly listOf(open.copy(id = openId))
        }
    }

    // A decision about nothing in particular: only the fields the tests vary are parameters.
    private fun decision(at: Long, features: List<Double>, outcome: DecisionOutcome?) = DecisionLogEntity(
        decidedAt = Instant.ofEpochMilli(at),
        visitId = null,
        transactionId = null,
        features = features,
        featureVersion = 1,
        action = ReminderAction.NOTIFY_NOW,
        probability = 0.8,
        modelVersion = "rules-1",
        outcome = outcome,
        outcomeAt = outcome?.let { Instant.ofEpochMilli(at + 60_000) },
    )
}
