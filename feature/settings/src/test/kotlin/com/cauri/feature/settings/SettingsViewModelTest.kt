package com.cauri.feature.settings

import com.cauri.core.capture.permission.FakePermissionService
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionStatus
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SettingsViewModelTest {
    private val permissions = FakePermissionService()

    @Nested
    inner class Status {
        @Test
        fun `should start with the status Android reports`() {
            // given
            val given = PermissionStatus(
                notificationAccess = true,
                notificationsAllowed = true,
                location = LocationAccess.ALWAYS,
            )
            permissions.status = given

            // when
            val viewModel = SettingsViewModel(permissions)

            // then
            viewModel.status.value shouldBe given
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should show the notifications allowed in the system settings`() {
            // given
            val viewModel = SettingsViewModel(permissions)
            permissions.status = permissions.status.copy(notificationsAllowed = true)

            // when
            viewModel.refresh()

            // then
            viewModel.status.value.notificationsAllowed.shouldBeTrue()
        }

        @Test
        fun `should show the location taken back in the system settings`() {
            // given
            permissions.status = permissions.status.copy(location = LocationAccess.ALWAYS)
            val viewModel = SettingsViewModel(permissions)
            permissions.status = permissions.status.copy(location = LocationAccess.DENIED)

            // when
            viewModel.refresh()

            // then
            viewModel.status.value.location shouldBe LocationAccess.DENIED
        }
    }
}
