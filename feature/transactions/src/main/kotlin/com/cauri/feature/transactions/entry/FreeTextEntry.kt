package com.cauri.feature.transactions.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

private val ButtonHeight = 48.dp
private val SendSize = 36.dp
private val ProgressSize = 20.dp

/**
 * « Ou écris : taxi 1500, beignets 500 », on the phones whose language model can read it: the expenses read, each
 * removable, then « Enregistrer 2 dépenses » on [accountId]; [onSaved] closes the sheet. Nothing shows without a model.
 */
@Composable
internal fun FreeTextEntry(accountId: Long?, onSaved: () -> Unit, viewModel: FreeTextViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.onSavedShown()
            onSaved()
        }
    }
    if (!state.available) return
    val palette = CauriTheme.palette
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        CauriTextField(
            value = state.text,
            onValueChange = viewModel::onText,
            missing = state.nothingFound,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { viewModel.onRead() }),
            placeholder = stringResource(R.string.transactions_free_text_hint),
            trailing = { ReadButton(reading = state.reading, onRead = viewModel::onRead) },
        )
        if (state.nothingFound) {
            Text(
                text = stringResource(R.string.transactions_free_text_nothing),
                style = MaterialTheme.typography.bodySmall,
                color = palette.coralText,
            )
        }
        state.lines?.let { lines ->
            Lines(lines = lines, onRemove = viewModel::onRemove)
            SaveButton(count = lines.size, enabled = accountId != null) { accountId?.let(viewModel::onSave) }
        }
    }
}

// The round lagoon arrow that has the model read the text, a spinner while it does.
@Composable
private fun ReadButton(reading: Boolean, onRead: () -> Unit) {
    val palette = CauriTheme.palette
    if (reading) {
        CircularProgressIndicator(modifier = Modifier.size(ProgressSize), color = palette.accent, strokeWidth = 2.dp)
    } else {
        val description = stringResource(R.string.transactions_free_text_read)
        Icon(
            painter = painterResource(DesignR.drawable.ic_check),
            contentDescription = description,
            tint = palette.onAccent,
            modifier = Modifier
                .size(SendSize)
                .background(palette.accent, CircleShape)
                .clickable(onClickLabel = description, role = Role.Button, onClick = onRead)
                .padding(CauriSpacing.s),
        )
    }
}

// One line per expense read: its label and category, its amount, and « Retirer ».
@Composable
private fun Lines(lines: List<FreeTextLine>, onRemove: (Int) -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = CauriSpacing.s)) {
            lines.forEachIndexed { index, line ->
                Row(
                    modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = line.label.replaceFirstChar(Char::titlecase),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = palette.ink,
                        )
                        Text(
                            text = line.categoryName ?: stringResource(R.string.transactions_free_text_no_category),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                    }
                    Text(
                        text = stringResource(R.string.transactions_free_text_amount, formatFcfa(line.amount)),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(R.string.transactions_free_text_remove),
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.coralText,
                        modifier = Modifier
                            .padding(start = CauriSpacing.m)
                            .clickable(role = Role.Button) { onRemove(index) }
                            .padding(CauriSpacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(count: Int, enabled: Boolean, onSave: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Button(
        onClick = {
            haptics.confirm()
            onSave()
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(ButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
    ) {
        Text(
            text = pluralStringResource(R.plurals.transactions_free_text_save, count, count),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
