package com.cauri

import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
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
internal class AppLockViewModelTest {
    private val preferences = FakeUserPreferencesRepository()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should lock a start once the lock is on`() {
            // given
            preferences.appLock.value = true

            // when
            val viewModel = AppLockViewModel(preferences)

            // then
            viewModel.uiState.value.locked.shouldBeTrue()
        }

        @Test
        fun `should not lock the user who just turned the lock on`() {
            // given
            val viewModel = AppLockViewModel(preferences)

            // when
            preferences.appLock.value = true

            // then
            viewModel.uiState.value.locked.shouldBeFalse()
        }
    }

    @Nested
    inner class OnBack {
        @Test
        fun `should lock again after more than a minute away`() {
            // given
            preferences.appLock.value = true
            val viewModel = AppLockViewModel(preferences).apply { onUnlocked() }
            viewModel.onLeft(at = 1_000)

            // when
            viewModel.onBack(at = 62_000)

            // then
            viewModel.uiState.value.locked.shouldBeTrue()
        }

        @Test
        fun `should not lock again within a minute`() {
            // given
            preferences.appLock.value = true
            val viewModel = AppLockViewModel(preferences).apply { onUnlocked() }
            viewModel.onLeft(at = 1_000)

            // when
            viewModel.onBack(at = 60_000)

            // then
            viewModel.uiState.value.locked.shouldBeFalse()
        }

        @Test
        fun `should never lock when the lock is off`() {
            // given
            val viewModel = AppLockViewModel(preferences)
            viewModel.onLeft(at = 1_000)

            // when
            viewModel.onBack(at = 600_000)

            // then
            viewModel.uiState.value.locked.shouldBeFalse()
        }
    }
}
