package com.cauri.feature.home

import com.cauri.core.capture.health.FakeCaptureHealthService
import com.cauri.core.data.repository.FakeRawSignalRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class CaptureHealthViewModelTest {
    private val rawSignals = FakeRawSignalRepository()
    private val health = FakeCaptureHealthService()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Silent {
        @Test
        fun `should warn while the capture is silent`() {
            // given
            health.silent = true

            // when
            val viewModel = CaptureHealthViewModel(rawSignals, health, clock)

            // then
            viewModel.silent.value.shouldBeTrue()
        }

        @Test
        fun `should show nothing while messages arrive`() {
            // when
            val viewModel = CaptureHealthViewModel(rawSignals, health, clock)

            // then
            viewModel.silent.value.shouldBeFalse()
        }
    }
}
