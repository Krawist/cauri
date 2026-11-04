package com.cauri.feature.settings

import com.cauri.core.capture.battery.FakeBatteryService
import com.cauri.core.capture.permission.FakePermissionService
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionStatus
import com.cauri.core.capture.security.FakeAppLockService
import com.cauri.core.capture.signal.FakeSignalProcessor
import com.cauri.core.data.model.AccountKind
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.NewAccount
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
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
internal class SettingsViewModelTest {
    private val permissions = FakePermissionService()
    private val battery = FakeBatteryService()
    private val preferences = FakeUserPreferencesRepository()
    private val processor = FakeSignalProcessor()
    private val lock = FakeAppLockService()

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
        fun `should start with the status Android reports`() {
            // given
            val given = PermissionStatus(
                notificationAccess = true,
                notificationsAllowed = true,
                location = LocationAccess.ALWAYS,
            )
            permissions.status = given
            battery.restricted = false

            // when
            val viewModel = SettingsViewModel(permissions, battery, preferences, processor, lock)

            // then
            viewModel.uiState.value.permissions shouldBe given
            viewModel.uiState.value.batteryUnrestricted.shouldBeTrue()
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should show the notifications allowed in the system settings`() {
            // given
            val viewModel = SettingsViewModel(permissions, battery, preferences, processor, lock)
            permissions.status = permissions.status.copy(notificationsAllowed = true)

            // when
            viewModel.refresh()

            // then
            viewModel.uiState.value.permissions.notificationsAllowed.shouldBeTrue()
        }

        @Test
        fun `should show the location taken back in the system settings`() {
            // given
            permissions.status = permissions.status.copy(location = LocationAccess.ALWAYS)
            val viewModel = SettingsViewModel(permissions, battery, preferences, processor, lock)
            permissions.status = permissions.status.copy(location = LocationAccess.DENIED)

            // when
            viewModel.refresh()

            // then
            viewModel.uiState.value.permissions.location shouldBe LocationAccess.DENIED
        }

        @Test
        fun `should show the battery restriction lifted in the system settings`() {
            // given
            val viewModel = SettingsViewModel(permissions, battery, preferences, processor, lock)
            battery.restricted = false

            // when
            viewModel.refresh()

            // then
            viewModel.uiState.value.batteryUnrestricted.shouldBeTrue()
        }
    }

    @Nested
    inner class OnReminderChange {
        @Test
        fun `should save the user's choice and show it`() {
            // given
            val viewModel = SettingsViewModel(permissions, battery, preferences, processor, lock)
            val chosen = CountReminderSettings(frequency = CountReminderFrequency.EVERY_EVENING, hour = 21)

            // when
            viewModel.onReminderChange(chosen)

            // then
            preferences.countReminder.value shouldBe chosen
            viewModel.uiState.value.countReminder shouldBe chosen
        }
    }

    @Nested
    inner class OnFollowAccount {
        @Test
        fun `should follow an ignored account and stop showing it as ignored`() {
            // given
            processor.ignoredAccounts.value = listOf(
                NewAccount(name = "UBA", kind = AccountKind.BANK, provider = "UBA", currency = "XAF"),
            )
            val viewModel = SettingsViewModel(permissions, battery, preferences, processor, lock)

            // when
            viewModel.onFollowAccount("UBA")

            // then
            processor.followed shouldContainExactly listOf("UBA")
            viewModel.uiState.value.ignoredAccounts.shouldBeEmpty()
        }
    }
}
