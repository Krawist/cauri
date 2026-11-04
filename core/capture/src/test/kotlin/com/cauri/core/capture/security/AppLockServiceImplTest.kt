package com.cauri.core.capture.security

import android.app.Activity
import com.cauri.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AppLockServiceImplTest {
    private val security = FakeDeviceSecurity()
    private val preferences = FakeUserPreferencesRepository()
    private val service = AppLockServiceImpl(security = security, preferences = preferences)

    @Nested
    inner class Set {
        @Test
        fun `should turn the lock off only once the user is recognised`() = runTest {
            // given
            preferences.appLock.value = true

            // when
            val done = service.set(on = false, activity = Activity())

            // then
            done.shouldBeTrue()
            preferences.appLock.value.shouldBeFalse()
            security.asked shouldBe 1
        }

        @Test
        fun `should leave the lock on when the user is not recognised`() = runTest {
            // given
            preferences.appLock.value = true
            security.recognised = false

            // when
            val done = service.set(on = false, activity = Activity())

            // then
            done.shouldBeFalse()
            preferences.appLock.value.shouldBeTrue()
        }

        @Test
        fun `should not turn the lock on without a screen lock on the phone`() = runTest {
            // given
            security.screenLock = false

            // when
            service.set(on = true, activity = Activity())

            // then
            preferences.appLock.value.shouldBeFalse()
        }
    }
}
