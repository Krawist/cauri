package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.RawSignalEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.RawSignal
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RawSignalRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: RawSignalRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = RawSignalRepositoryImpl(database.rawSignalDao())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Record {
        @Test
        fun `should keep the notification as received, not parsed yet`() = runTest {
            // when
            val kept = repository.record(
                sender = "com.google.android.apps.messaging",
                title = "MobileMoney",
                text = "Retrait de 20000 FCFA",
                receivedAt = Instant.ofEpochSecond(1_000),
            )

            // then
            kept.shouldBeTrue()
            database.rawSignalDao().getUnparsed().map { it.copy(id = 0) } shouldContainExactly listOf(
                RawSignalEntity(
                    sender = "com.google.android.apps.messaging",
                    title = "MobileMoney",
                    text = "Retrait de 20000 FCFA",
                    receivedAt = Instant.ofEpochSecond(1_000),
                    templateVersion = null,
                ),
            )
        }

        @Test
        fun `should keep the same notification posted again only once`() = runTest {
            // given
            repository.record(
                sender = "com.ubanquity.redd.uba",
                title = null,
                text = "Debit 5000",
                receivedAt = Instant.ofEpochSecond(1_000),
            )

            // when
            val kept = repository.record(
                sender = "com.ubanquity.redd.uba",
                title = null,
                text = "Debit 5000",
                receivedAt = Instant.ofEpochSecond(1_060),
            )

            // then
            kept.shouldBeFalse()
            database.rawSignalDao().getUnparsed() shouldHaveSize 1
        }

        @Test
        fun `should keep the same text again once it is not a repost any more`() = runTest {
            // given
            val first = Instant.ofEpochSecond(1_000)
            repository.record(sender = "com.ubanquity.redd.uba", title = null, text = "Debit 5000", receivedAt = first)

            // when
            val kept = repository.record(
                sender = "com.ubanquity.redd.uba",
                title = null,
                text = "Debit 5000",
                receivedAt = first + Duration.ofHours(1),
            )

            // then
            kept.shouldBeTrue()
        }
    }

    @Nested
    inner class Unparsed {
        @Test
        fun `should give the signals no template read yet, oldest first`() = runTest {
            // given
            val later = database.rawSignalDao().insert(
                RawSignalEntity(
                    sender = "com.samsung.android.messaging",
                    title = "OrangeMoney",
                    text = "Paiement de 2500 FCFA",
                    receivedAt = Instant.ofEpochSecond(2_000),
                    templateVersion = null,
                ),
            )
            database.rawSignalDao().insert(
                RawSignalEntity(
                    sender = "com.samsung.android.messaging",
                    title = "OrangeMoney",
                    text = "Retrait de 10000 FCFA",
                    receivedAt = Instant.ofEpochSecond(500),
                    templateVersion = "orange-money.withdrawal@1",
                ),
            )
            val earlier = database.rawSignalDao().insert(
                RawSignalEntity(
                    sender = "com.samsung.android.messaging",
                    title = "UBA",
                    text = "DEBIT",
                    receivedAt = Instant.ofEpochSecond(1_000),
                    templateVersion = null,
                ),
            )

            // when
            val signals = repository.unparsed()

            // then
            signals shouldContainExactly listOf(
                RawSignal(
                    id = earlier,
                    sender = "com.samsung.android.messaging",
                    title = "UBA",
                    text = "DEBIT",
                    receivedAt = Instant.ofEpochSecond(1_000),
                ),
                RawSignal(
                    id = later,
                    sender = "com.samsung.android.messaging",
                    title = "OrangeMoney",
                    text = "Paiement de 2500 FCFA",
                    receivedAt = Instant.ofEpochSecond(2_000),
                ),
            )
        }
    }

    @Nested
    inner class LastReceivedAt {
        @Test
        fun `should give when the last signal was received`() = runTest {
            // given
            repository.record(
                sender = "test.sms",
                title = "UBA",
                text = "Frais",
                receivedAt = Instant.ofEpochSecond(2_000),
            )
            repository.record(
                sender = "test.sms",
                title = "UBA",
                text = "Debit",
                receivedAt = Instant.ofEpochSecond(1_000),
            )

            // when
            val last = repository.lastReceivedAt()

            // then
            last shouldBe Instant.ofEpochSecond(2_000)
        }

        @Test
        fun `should give nothing before the first signal`() = runTest {
            // when
            val last = repository.lastReceivedAt()

            // then
            last.shouldBeNull()
        }
    }
}
