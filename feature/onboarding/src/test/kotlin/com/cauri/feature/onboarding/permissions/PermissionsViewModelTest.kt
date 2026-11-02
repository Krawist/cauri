package com.cauri.feature.onboarding.permissions

import com.cauri.core.capture.permission.FakePermissionService
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.capture.permission.PermissionStatus
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PermissionsViewModelTest {
    private val permissions = FakePermissionService()

    @Nested
    inner class Status {
        @Test
        fun `should start with the status Android reports`() {
            // given
            val given = PermissionStatus(
                notificationAccess = true,
                notificationsAllowed = false,
                location = LocationAccess.WHILE_IN_USE,
            )
            permissions.status = given

            // when
            val viewModel = PermissionsViewModel(permissions)

            // then
            viewModel.status.value shouldBe given
        }
    }

    @Nested
    inner class Refresh {
        @Test
        fun `should show the access given in the system settings`() {
            // given
            val viewModel = PermissionsViewModel(permissions)
            permissions.status = permissions.status.copy(notificationAccess = true)

            // when
            viewModel.refresh()

            // then
            viewModel.status.value.notificationAccess.shouldBeTrue()
        }

        @Test
        fun `should show the location allowed in the background`() {
            // given
            val viewModel = PermissionsViewModel(permissions)
            permissions.status = permissions.status.copy(location = LocationAccess.ALWAYS)

            // when
            viewModel.refresh()

            // then
            viewModel.status.value.location shouldBe LocationAccess.ALWAYS
        }
    }
}
