package com.cauri.core.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cauri.core.data.model.IncomeType
import com.cauri.core.data.model.Occupation
import com.cauri.core.data.model.UserProfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
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

        @Test
        fun `should return the saved profile`() = scope.runTest {
            // given
            val saved =
                UserProfile(displayName = "Krawist", occupation = Occupation.TRADER, incomeType = IncomeType.VARIABLE)
            repository.saveProfile(saved)

            // when
            val profile = repository.profile.first()

            // then
            profile shouldBe saved
        }
    }
}
