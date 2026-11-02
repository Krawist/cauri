package com.cauri.feature.settings

import com.cauri.core.capture.battery.FakeBatteryService
import com.cauri.core.capture.permission.FakePermissionService
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionStatus
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SettingsViewModelTest {
    private val permissions = FakePermissionService()
    private val battery = FakeBatteryService()

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
            val viewModel = SettingsViewModel(permissions, battery)

            // then
            viewModel.uiState.value shouldBe SettingsUiState(permissions = given, batteryUnrestricted = true)
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should show the notifications allowed in the system settings`() {
            // given
            val viewModel = SettingsViewModel(permissions, battery)
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
            val viewModel = SettingsViewModel(permissions, battery)
            permissions.status = permissions.status.copy(location = LocationAccess.DENIED)

            // when
            viewModel.refresh()

            // then
            viewModel.uiState.value.permissions.location shouldBe LocationAccess.DENIED
        }

        @Test
        fun `should show the battery restriction lifted in the system settings`() {
            // given
            val viewModel = SettingsViewModel(permissions, battery)
            battery.restricted = false

            // when
            viewModel.refresh()

            // then
            viewModel.uiState.value.batteryUnrestricted.shouldBeTrue()
        }
    }
}
