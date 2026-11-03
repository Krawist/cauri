package com.cauri.core.capture.health

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CaptureHealthServiceImplTest {
    private val service = CaptureHealthServiceImpl()

    @Nested
    inner class Silent {
        @Test
        fun `should be silent a month after the last message`() {
            // when
            val silent = service.silent(lastSignal = NOW.minus(Duration.ofDays(30)), now = NOW)

            // then
            silent.shouldBeTrue()
        }

        @Test
        fun `should not be silent within a month, a month without payment being usual`() {
            // when
            val silent = service.silent(lastSignal = NOW.minus(Duration.ofDays(29)), now = NOW)

            // then
            silent.shouldBeFalse()
        }

        @Test
        fun `should never be silent before the first message`() {
            // when
            val silent = service.silent(lastSignal = null, now = NOW)

            // then
            silent.shouldBeFalse()
        }
    }

    @Nested
    inner class ShouldWarn {
        @Test
        fun `should warn when silent while a permission is missing, never warned`() {
            // when
            val warn = service.shouldWarn(
                lastSignal = NOW.minus(Duration.ofDays(40)),
                lastWarning = null,
                blocked = true,
                now = NOW,
            )

            // then
            warn.shouldBeTrue()
        }

        @Test
        fun `should warn again a week after the last warning`() {
            // when
            val warn = service.shouldWarn(
                lastSignal = NOW.minus(Duration.ofDays(40)),
                lastWarning = NOW.minus(Duration.ofDays(7)),
                blocked = true,
                now = NOW,
            )

            // then
            warn.shouldBeTrue()
        }

        @Test
        fun `should not warn twice in a week`() {
            // when
            val warn = service.shouldWarn(
                lastSignal = NOW.minus(Duration.ofDays(40)),
                lastWarning = NOW.minus(Duration.ofDays(6)),
                blocked = true,
                now = NOW,
            )

            // then
            warn.shouldBeFalse()
        }

        @Test
        fun `should not warn when every permission is given, the silence being the user's rhythm`() {
            // when
            val warn = service.shouldWarn(
                lastSignal = NOW.minus(Duration.ofDays(40)),
                lastWarning = null,
                blocked = false,
                now = NOW,
            )

            // then
            warn.shouldBeFalse()
        }

        @Test
        fun `should not warn while messages arrive`() {
            // when
            val warn = service.shouldWarn(
                lastSignal = NOW.minus(Duration.ofDays(2)),
                lastWarning = null,
                blocked = true,
                now = NOW,
            )

            // then
            warn.shouldBeFalse()
        }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-03T12:00:00Z")
    }
}
