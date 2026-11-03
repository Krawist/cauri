package com.cauri.feature.home

import com.cauri.core.capture.battery.FakeBatteryService
import com.cauri.core.capture.health.FakeCaptureHealthService
import com.cauri.core.capture.permission.FakePermissionService
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
    private val permissions = FakePermissionService()
    private val battery = FakeBatteryService()
    private val clock = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC)
    private val viewModel = CaptureHealthViewModel(rawSignals, health, permissions, battery, clock)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should warn while the capture is silent and a permission is missing`() {
            // given
            health.silent = true
            permissions.status = permissions.status.copy(notificationAccess = true)

            // when
            viewModel.refresh()

            // then
            viewModel.paused.value.shouldBeTrue()
        }

        @Test
        fun `should show nothing while silent with every permission given`() {
            // given
            health.silent = true
            permissions.status = permissions.status.copy(notificationAccess = true)
            battery.restricted = false

            // when
            viewModel.refresh()

            // then
            viewModel.paused.value.shouldBeFalse()
        }

        @Test
        fun `should show nothing while messages arrive`() {
            // when
            viewModel.refresh()

            // then
            viewModel.paused.value.shouldBeFalse()
        }

        @Test
        fun `should hide the card once the permissions are back`() {
            // given
            health.silent = true
            viewModel.refresh()
            permissions.status = permissions.status.copy(notificationAccess = true)
            battery.restricted = false

            // when
            viewModel.refresh()

            // then
            viewModel.paused.value.shouldBeFalse()
        }
    }
}
