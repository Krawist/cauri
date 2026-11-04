package com.cauri.feature.onboarding.security

import android.app.Activity
import com.cauri.core.capture.security.FakeAppLockService
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
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
internal class SecurityViewModelTest {
    private val lock = FakeAppLockService()
    private lateinit var viewModel: SecurityViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SecurityViewModel(lock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnEnable {
        @Test
        fun `should turn the lock on and go on once the user is recognised`() {
            // given
            var done = 0

            // when
            viewModel.onEnable(Activity()) { done++ }

            // then
            viewModel.uiState.value.enabled.shouldBeTrue()
            done shouldBe 1
        }

        @Test
        fun `should stay on the step when the user is not recognised`() {
            // given
            lock.recognised = false
            var done = 0

            // when
            viewModel.onEnable(Activity()) { done++ }

            // then
            viewModel.uiState.value.enabled.shouldBeFalse()
            done shouldBe 0
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should read again whether the phone has a screen lock`() {
            // given
            lock.screenLock = false

            // when
            viewModel.refresh()

            // then
            viewModel.uiState.value.available.shouldBeFalse()
        }
    }
}
