package com.cauri.feature.settings.backup

import androidx.lifecycle.SavedStateHandle
import com.cauri.core.data.model.RestoreResult
import com.cauri.core.data.repository.FakeBackupRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class BackupViewModelTest {
    private val backups = FakeBackupRepository()
    private lateinit var viewModel: BackupViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = BackupViewModel(backups = backups, savedStateHandle = SavedStateHandle())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should offer only the restore from the onboarding`() {
            // when
            val viewModel = BackupViewModel(backups, SavedStateHandle(mapOf("restoreOnly" to true)))

            // then
            viewModel.uiState.value.restoreOnly.shouldBeTrue()
        }
    }

    @Nested
    inner class OnAskExport {
        @Test
        fun `should refuse a passphrase shorter than 8 characters`() {
            // given
            viewModel.onPassphrase("kribi")
            viewModel.onConfirmation("kribi")

            // when
            val asked = viewModel.onAskExport()

            // then
            asked.shouldBeFalse()
            viewModel.uiState.value.message shouldBe BackupMessage.TOO_SHORT
        }

        @Test
        fun `should refuse two passphrases that differ`() {
            // given
            viewModel.onPassphrase("kribi plage")
            viewModel.onConfirmation("kribi plages")

            // when
            val asked = viewModel.onAskExport()

            // then
            asked.shouldBeFalse()
            viewModel.uiState.value.message shouldBe BackupMessage.MISMATCH
        }
    }

    @Nested
    inner class OnExport {
        @Test
        fun `should write the backup with the passphrase`() {
            // given
            viewModel.onPassphrase("kribi plage")

            // when
            viewModel.onExport { ByteArrayOutputStream() }

            // then
            backups.exported shouldContainExactly listOf("kribi plage")
            viewModel.uiState.value.message shouldBe BackupMessage.SAVED
        }

        @Test
        fun `should tell a file that cannot be written`() {
            // when
            viewModel.onExport { null }

            // then
            viewModel.uiState.value.message shouldBe BackupMessage.FAILED
        }
    }

    @Nested
    inner class OnConfirmRestore {
        @Test
        fun `should restore only once the user agreed`() {
            // given
            viewModel.onPassphrase("kribi plage")

            // when
            viewModel.onAskRestore { ByteArrayInputStream(ByteArray(0)) }

            // then
            viewModel.uiState.value.confirmingRestore.shouldBeTrue()
            backups.restored.shouldBeEmpty()
        }

        @Test
        fun `should restore the backup and start again`() {
            // given
            viewModel.onPassphrase("kribi plage")
            viewModel.onAskRestore { ByteArrayInputStream(ByteArray(0)) }

            // when
            viewModel.onConfirmRestore()

            // then
            backups.restored shouldContainExactly listOf("kribi plage")
            viewModel.uiState.value.restored.shouldBeTrue()
        }

        @ParameterizedTest
        @EnumSource(RestoreResult::class, names = ["RESTORED"], mode = EnumSource.Mode.EXCLUDE)
        fun `should tell why a backup was not restored`(result: RestoreResult) {
            // given
            backups.restoreResult = result
            viewModel.onAskRestore { ByteArrayInputStream(ByteArray(0)) }

            // when
            viewModel.onConfirmRestore()

            // then
            viewModel.uiState.value.message shouldBe BackupMessage.valueOf(result.name)
            viewModel.uiState.value.restored.shouldBeFalse()
        }
    }
}
