package com.cauri.feature.settings.backup

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.BackButton
import com.cauri.feature.settings.R
import java.time.LocalDate
import kotlin.system.exitProcess
import kotlinx.coroutines.delay

private val ButtonHeight = 56.dp

// Long enough to read « Données restaurées » before the app starts again.
private const val RESTART_DELAY = 1_500L

/**
 * « Sauvegarde chiffrée »: the passphrase, typed twice to back up, « Sauvegarder dans un fichier » (the file chosen
 * with Android's picker: the phone, a memory card, Drive…) and « Restaurer une sauvegarde », which asks before
 * replacing the data of the phone, then starts the app again. From the onboarding, only the restore.
 */
@Composable
internal fun BackupScreen(onBack: () -> Unit, viewModel: BackupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MIME_TYPE)) { uri ->
        uri?.let { viewModel.onExport { context.contentResolver.openOutputStream(it) } }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onAskRestore { context.contentResolver.openInputStream(it) } }
    }
    LaunchedEffect(state.restored) {
        if (state.restored) {
            delay(RESTART_DELAY)
            restart(context)
        }
    }
    BackupContent(
        state = state,
        onBack = onBack,
        onPassphrase = viewModel::onPassphrase,
        onConfirmation = viewModel::onConfirmation,
        onExport = { if (viewModel.onAskExport()) create.launch(fileName()) },
        onRestore = { open.launch(arrayOf("*/*")) },
    )
    if (state.confirmingRestore) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelRestore,
            containerColor = CauriTheme.palette.background,
            title = { Text(stringResource(R.string.backup_restore_confirm_title)) },
            text = { Text(stringResource(R.string.backup_restore_confirm_text)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmRestore) {
                    Text(stringResource(R.string.backup_restore_confirm), color = CauriTheme.palette.coralText)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelRestore) {
                    Text(stringResource(R.string.backup_cancel), color = CauriTheme.palette.muted)
                }
            },
        )
    }
}

// Parameters: one per field and button of the screen.
@Suppress("LongParameterList")
@Composable
private fun BackupContent(
    state: BackupUiState,
    onBack: () -> Unit,
    onPassphrase: (String) -> Unit,
    onConfirmation: (String) -> Unit,
    onExport: () -> Unit,
    onRestore: () -> Unit,
) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
    ) {
        BackButton(onClick = onBack)
        Text(
            text = stringResource(R.string.backup_title),
            style = MaterialTheme.typography.headlineLarge,
            color = palette.ink,
            modifier = Modifier.padding(top = CauriSpacing.l),
        )
        Text(
            text = stringResource(if (state.restoreOnly) R.string.backup_restore_text else R.string.backup_text),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
        )
        PassphraseField(
            value = state.passphrase,
            label = stringResource(R.string.backup_passphrase),
            onValueChange = onPassphrase,
        )
        if (!state.restoreOnly) {
            PassphraseField(
                value = state.confirmation,
                label = stringResource(R.string.backup_passphrase_again),
                onValueChange = onConfirmation,
            )
        }
        state.message?.let { Message(it) }
        if (state.restored) Message(text = stringResource(R.string.backup_restored), error = false)
        Actions(state = state, onExport = onExport, onRestore = onRestore)
        Text(
            text = stringResource(R.string.backup_hint),
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted,
        )
    }
}

// « Sauvegarder dans un fichier », then « Restaurer une sauvegarde » once a passphrase is typed.
@Composable
private fun Actions(state: BackupUiState, onExport: () -> Unit, onRestore: () -> Unit) {
    val palette = CauriTheme.palette
    if (!state.restoreOnly) {
        Button(
            onClick = onExport,
            enabled = !state.working,
            modifier = Modifier.fillMaxWidth().height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) { Text(stringResource(R.string.backup_export), style = MaterialTheme.typography.titleMedium) }
    }
    OutlinedButton(
        onClick = onRestore,
        enabled = !state.working && state.passphrase.isNotEmpty(),
        modifier = Modifier.fillMaxWidth().height(ButtonHeight),
        shape = CircleShape,
    ) {
        Text(
            stringResource(R.string.backup_restore),
            style = MaterialTheme.typography.titleMedium,
            color = palette.accentText,
        )
    }
}

@Composable
private fun PassphraseField(value: String, label: String, onValueChange: (String) -> Unit) {
    CauriTextField(
        value = value,
        onValueChange = onValueChange,
        missing = false,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = PasswordVisualTransformation(),
        placeholder = label,
    )
}

@Composable
private fun Message(message: BackupMessage) {
    Message(
        text = stringResource(
            when (message) {
                BackupMessage.SAVED -> R.string.backup_saved
                BackupMessage.TOO_SHORT -> R.string.backup_too_short
                BackupMessage.MISMATCH -> R.string.backup_mismatch
                BackupMessage.WRONG_PASSPHRASE -> R.string.backup_wrong_passphrase
                BackupMessage.NOT_A_BACKUP -> R.string.backup_not_a_backup
                BackupMessage.TOO_NEW -> R.string.backup_too_new
                BackupMessage.FAILED -> R.string.backup_failed
            },
        ),
        error = message != BackupMessage.SAVED,
    )
}

@Composable
private fun Message(text: String, error: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (error) CauriTheme.palette.coralText else CauriTheme.palette.greenText,
    )
}

// « cauri-2026-11-04.cauri ».
private fun fileName() = "cauri-${LocalDate.now()}.cauri"

// The data were replaced under the running app: it starts again from the launcher, in a new process.
private fun restart(context: Context) {
    context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
        context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }
    exitProcess(0)
}

private const val MIME_TYPE = "application/octet-stream"

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BackupContentPreview() {
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            BackupContent(
                state = BackupUiState(passphrase = "kribi plage", message = BackupMessage.MISMATCH),
                onBack = {},
                onPassphrase = {},
                onConfirmation = {},
                onExport = {},
                onRestore = {},
            )
        }
    }
}
