package com.cauri.feature.settings.backup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.data.model.RestoreResult
import com.cauri.core.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « Sauvegarde chiffrée »: the passphrase, the backup written to the file the user chose, and the restore of one,
 * after the user agreed to replace the data of the phone.
 */
@HiltViewModel
internal class BackupViewModel @Inject constructor(
    private val backups: BackupRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // The argument of [com.cauri.feature.settings.BackupRoute].
    private val state = MutableStateFlow(
        BackupUiState(restoreOnly = savedStateHandle.get<Boolean>("restoreOnly") == true),
    )

    val uiState: StateFlow<BackupUiState> = state.asStateFlow()

    // The backup chosen to restore, while the user is asked to agree.
    private var pending: (() -> InputStream?)? = null

    fun onPassphrase(text: String) = state.update { it.copy(passphrase = text, message = null) }

    fun onConfirmation(text: String) = state.update { it.copy(confirmation = text, message = null) }

    // What is wrong with the passphrase to lock a backup; `null` when nothing.
    private fun exportProblem(): BackupMessage? {
        val current = state.value
        return when {
            current.passphrase.length < MIN_LENGTH -> BackupMessage.TOO_SHORT
            current.passphrase != current.confirmation -> BackupMessage.MISMATCH
            else -> null
        }
    }

    /** Points out what is wrong with the passphrase before the file is chosen; `true` when the backup can go on. */
    fun onAskExport(): Boolean {
        val problem = exportProblem()
        state.update { it.copy(message = problem) }
        return problem == null
    }

    /** Writes the backup to the file the user created, which [open] opens; `null` when it cannot. */
    fun onExport(open: () -> OutputStream?) = run { passphrase ->
        backups.export(open() ?: throw IOException("The file chosen cannot be written"), passphrase)
        BackupMessage.SAVED
    }

    /** Asks to replace the data of the phone by the backup [open] opens; [onConfirmRestore] goes on. */
    fun onAskRestore(open: () -> InputStream?) {
        pending = open
        state.update { it.copy(confirmingRestore = true, message = null) }
    }

    fun onCancelRestore() = state.update { it.copy(confirmingRestore = false) }

    /** Replaces the data of the phone by the backup chosen. */
    fun onConfirmRestore() {
        val open = pending ?: return
        state.update { it.copy(confirmingRestore = false) }
        run { passphrase ->
            val input = open() ?: throw IOException("The file chosen cannot be read")
            when (backups.restore(input, passphrase)) {
                RestoreResult.RESTORED -> {
                    state.update { it.copy(restored = true) }
                    null
                }
                RestoreResult.WRONG_PASSPHRASE -> BackupMessage.WRONG_PASSPHRASE
                RestoreResult.NOT_A_BACKUP -> BackupMessage.NOT_A_BACKUP
                RestoreResult.TOO_NEW -> BackupMessage.TOO_NEW
            }
        }
    }

    // Runs [action] with the passphrase while « working », then tells its message; a file that cannot be read or
    // written is « FAILED ».
    private fun run(action: suspend (CharArray) -> BackupMessage?) {
        state.update { it.copy(working = true, message = null) }
        viewModelScope.launch {
            val message = try {
                action(state.value.passphrase.toCharArray())
            } catch (_: IOException) {
                BackupMessage.FAILED
            }
            state.update { it.copy(working = false, message = message) }
        }
    }

    private companion object {
        const val MIN_LENGTH = 8
    }
}
