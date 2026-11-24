package com.cauri.feature.transactions.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Account
import com.cauri.core.data.model.AccountKind
import com.cauri.core.design.format.formatMoment
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

private val RowHeight = 52.dp

/**
 * The account (both for a transfer), the date and the note, one row each as in the mockup. The account of an
 * operation read in a message is shown, never changed: the message proves it.
 */
@Composable
internal fun EditDetails(state: EditUiState, actions: EditActions) {
    val transaction = state.transaction ?: return
    val palette = CauriTheme.palette
    var choosingMoment by remember { mutableStateOf(false) }
    val account = state.accounts.firstOrNull { it.id == state.accountId }
    GlassSurface(shape = RoundedCornerShape(CauriRadius.tile), strong = true, modifier = Modifier.fillMaxWidth()) {
        Column {
            val to = transaction.toAccountId?.let { id -> state.accounts.firstOrNull { it.id == id } }
            if (to != null) {
                DetailRow(
                    label = stringResource(R.string.transactions_from),
                    value = account?.name,
                    color = account.tint(),
                )
                Divider()
                DetailRow(label = stringResource(R.string.transactions_to), value = to.name, color = to.tint())
            } else {
                AccountRow(
                    account = account,
                    // The accounts to move it to, when the message does not prove its own.
                    accounts = state.accounts.takeUnless { transaction.read }.orEmpty(),
                    onAccount = actions::onAccount,
                )
            }
            Divider()
            val locale = LocalConfiguration.current.locales[0]
            DetailRow(
                label = stringResource(R.string.transactions_edit_date),
                value = formatMoment(state.occurredAt).replaceFirstChar { it.titlecase(locale) },
                color = palette.ink,
                onClick = { choosingMoment = true },
            )
            Divider()
            NoteRow(note = state.note, onNote = actions::onNote)
        }
    }
    if (choosingMoment) {
        MomentDialog(
            moment = state.occurredAt,
            onMoment = {
                actions.onOccurredAt(it)
                choosingMoment = false
            },
            onDismiss = { choosingMoment = false },
        )
    }
}

// « Compte », and the menu of the other accounts when it can be changed.
@Composable
private fun AccountRow(account: Account?, accounts: List<Account>, onAccount: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        DetailRow(
            label = stringResource(R.string.transactions_edit_account),
            value = account?.name,
            color = account.tint(),
            onClick = if (accounts.size > 1) ({ open = true }) else null,
        )
        DropdownMenu(expanded = open, onDismissRequest = {
            open = false
        }, containerColor = CauriTheme.palette.background) {
            accounts.forEach {
                DropdownMenuItem(
                    text = { Text(text = it.name, color = it.tint()) },
                    onClick = {
                        onAccount(it.id)
                        open = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?, color: Color, onClick: (() -> Unit)? = null) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = CauriSpacing.l),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = palette.muted)
        Text(
            text = value.orEmpty(),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

// « Note », typed on the right of its row; a hint until then.
@Composable
private fun NoteRow(note: String, onNote: (String) -> Unit) {
    val palette = CauriTheme.palette
    val style = MaterialTheme.typography.bodyLarge.copy(color = palette.ink, textAlign = TextAlign.End)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = RowHeight).padding(horizontal = CauriSpacing.l),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.transactions_edit_note),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.muted,
        )
        BasicTextField(
            value = note,
            onValueChange = onNote,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(palette.accent),
            modifier = Modifier.weight(1f),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (note.isEmpty()) {
                        Text(
                            text = stringResource(R.string.transactions_edit_note_hint),
                            style = style.copy(color = palette.muted),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    field()
                }
            },
        )
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CauriTheme.palette.chip))
}

// Gold for cash, as everywhere.
@Composable
private fun Account?.tint() = if (this?.kind ==
    AccountKind.CASH
) {
    CauriTheme.palette.goldText
} else {
    CauriTheme.palette.ink
}
