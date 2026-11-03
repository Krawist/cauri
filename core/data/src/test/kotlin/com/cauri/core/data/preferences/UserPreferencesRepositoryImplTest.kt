package com.cauri.core.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cauri.core.data.model.CountReminderFrequency
import com.cauri.core.data.model.CountReminderSettings
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.PaymentHabit
import com.cauri.core.data.model.UserProfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class UserPreferencesRepositoryImplTest {
    @TempDir
    lateinit var directory: File

    private val scope = TestScope()
    private lateinit var repository: UserPreferencesRepositoryImpl

    @BeforeEach
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            File(directory, "preferences.preferences_pb")
        }
        repository = UserPreferencesRepositoryImpl(dataStore)
    }

    @Nested
    inner class Profile {
        @Test
        fun `should be null before the profile is saved`() = scope.runTest {
            // when
            val profile = repository.profile.first()

            // then
            profile.shouldBeNull()
        }
    }

    @Nested
    inner class SaveProfile {
        @Test
        fun `should save the whole profile`() = scope.runTest {
            // given
            val saved = UserProfile(
                displayName = "Krawist",
                occupation = Occupation.TRADER,
                incomeType = IncomeType.VARIABLE,
                paymentHabit = PaymentHabit.MOSTLY_CASH,
            )

            // when
            repository.saveProfile(saved)

            // then
            repository.profile.first() shouldBe saved
        }

        @Test
        fun `should replace the stored profile`() = scope.runTest {
            // given
            val first = UserProfile(
                displayName = "Krawist",
                occupation = Occupation.TRADER,
                incomeType = IncomeType.VARIABLE,
                paymentHabit = PaymentHabit.MOSTLY_CASH,
            )
            repository.saveProfile(first)
            val second = first.copy(occupation = Occupation.EMPLOYEE, paymentHabit = PaymentHabit.BOTH)

            // when
            repository.saveProfile(second)

            // then
            repository.profile.first() shouldBe second
        }
    }

    @Nested
    inner class CountReminder {
        @Test
        fun `should remind when useful, from 20 h, until the user chooses`() = scope.runTest {
            // when
            val settings = repository.countReminder.first()

            // then
            settings shouldBe CountReminderSettings(frequency = CountReminderFrequency.WHEN_USEFUL, hour = 20)
        }

        @Test
        fun `should keep the user's choice`() = scope.runTest {
            // given
            val chosen = CountReminderSettings(frequency = CountReminderFrequency.EVERY_EVENING, hour = 21)

            // when
            repository.saveCountReminder(chosen)

            // then
            repository.countReminder.first() shouldBe chosen
        }
    }

    @Nested
    inner class LastCountReminder {
        @Test
        fun `should keep the day of the last reminder`() = scope.runTest {
            // when
            repository.setLastCountReminder(LocalDate.of(2026, 11, 3))

            // then
            repository.lastCountReminder.first() shouldBe LocalDate.of(2026, 11, 3)
        }
    }

    @Nested
    inner class ProposeAccount {
        @Test
        fun `should offer to follow the provider`() = scope.runTest {
            // when
            repository.proposeAccount("ORANGE_MONEY")

            // then
            repository.proposedAccounts.first() shouldBe setOf("ORANGE_MONEY")
        }

        @Test
        fun `should not offer again a provider the user ignored`() = scope.runTest {
            // given
            repository.ignoreAccount("ORANGE_MONEY")

            // when
            repository.proposeAccount("ORANGE_MONEY")

            // then
            repository.proposedAccounts.first() shouldBe emptySet()
        }
    }

    @Nested
    inner class IgnoreAccount {
        @Test
        fun `should move the offer to the ignored providers`() = scope.runTest {
            // given
            repository.proposeAccount("ORANGE_MONEY")

            // when
            repository.ignoreAccount("ORANGE_MONEY")

            // then
            repository.proposedAccounts.first() shouldBe emptySet()
            repository.ignoredAccounts.first() shouldBe setOf("ORANGE_MONEY")
        }
    }

    @Nested
    inner class ClearAccountChoice {
        @Test
        fun `should forget the offer and the refusal`() = scope.runTest {
            // given
            repository.proposeAccount("UBA")
            repository.ignoreAccount("ORANGE_MONEY")

            // when
            repository.clearAccountChoice("UBA")
            repository.clearAccountChoice("ORANGE_MONEY")

            // then
            repository.proposedAccounts.first() shouldBe emptySet()
            repository.ignoredAccounts.first() shouldBe emptySet()
        }
    }

    @Nested
    inner class SetLastCaptureWarning {
        @Test
        fun `should remember when the user was warned`() = scope.runTest {
            // when
            repository.setLastCaptureWarning(Instant.parse("2026-11-03T12:00:00Z"))

            // then
            repository.lastCaptureWarning.first() shouldBe Instant.parse("2026-11-03T12:00:00Z")
        }
    }
}
