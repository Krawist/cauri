package com.cauri.feature.onboarding.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Bank
import io.github.krawist.design.component.BackdropShape
import com.cauri.core.design.component.CauriTextField
import io.github.krawist.design.component.backdrop
import io.github.krawist.design.glass.GlassScreen
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.onboarding.R

private val ButtonHeight = 56.dp

// Larger shapes than on the screens, placed for a tall sheet: every card has colour behind it.
private const val LAGOON_X = 0.9f
private const val LAGOON_Y = 0.12f
private const val GOLD_X = 0.05f
private const val GOLD_Y = 0.5f
private const val CORAL_X = 0.85f
private const val CORAL_Y = 0.9f
private const val SHAPE_RADIUS = 0.8f

// A bank that cannot be chosen any more, once the maximum is reached.
private const val UNAVAILABLE_ALPHA = 0.45f

/**
 * List of banks, searchable, where the user chooses up to [MAX_BANKS] at once. It is a glass screen of its own: its
 * cards frost the coloured shapes drawn behind them, as on the screens, whose shapes are out of reach behind the
 * sheet's window.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BankPicker(state: AccountsUiState, actions: AccountsActions) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = actions::onBankPickerClose,
        containerColor = palette.background,
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        GlassScreen(
            background = {
                Box(
                    Modifier.fillMaxSize().backdrop(
                        listOf(
                            BackdropShape(
                                color = palette.blobLagoon,
                                x = LAGOON_X,
                                y = LAGOON_Y,
                                radius = SHAPE_RADIUS,
                            ),
                            BackdropShape(color = palette.blobGold, x = GOLD_X, y = GOLD_Y, radius = SHAPE_RADIUS),
                            BackdropShape(color = palette.blobCoral, x = CORAL_X, y = CORAL_Y, radius = SHAPE_RADIUS),
                        ),
                    ),
                )
            },
        ) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                BankPickerHeader(state = state, actions = actions)
                BankList(state = state, actions = actions, modifier = Modifier.weight(1f))
                Button(
                    onClick = actions::onBankPickerClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l)
                        .height(ButtonHeight),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accent,
                        contentColor = palette.onAccent,
                    ),
                ) {
                    Text(
                        text = pluralStringResource(R.plurals.accounts_banks_done, state.banks.size, state.banks.size),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun BankPickerHeader(state: AccountsUiState, actions: AccountsActions) {
    Column(
        modifier = Modifier.padding(horizontal = CauriSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
    ) {
        Text(text = stringResource(R.string.accounts_pick_bank), style = MaterialTheme.typography.headlineSmall)
        CauriTextField(
            value = state.bankQuery,
            onValueChange = actions::onBankQueryChange,
            missing = false,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Search,
            ),
            placeholder = stringResource(R.string.accounts_search_bank),
        )
        if (state.banks.size >= MAX_BANKS) {
            Text(
                text = stringResource(R.string.accounts_banks_max_reached),
                style = MaterialTheme.typography.bodySmall,
                color = CauriTheme.palette.muted,
            )
        }
    }
}

@Composable
private fun BankList(state: AccountsUiState, actions: AccountsActions, modifier: Modifier = Modifier) {
    if (state.bankResults.isEmpty()) {
        Text(
            text = stringResource(R.string.accounts_no_bank_found),
            style = MaterialTheme.typography.bodyMedium,
            color = CauriTheme.palette.muted,
            modifier = modifier.padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.xl),
        )
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
    ) {
        items(state.bankResults, key = Bank::code) { bank ->
            val chosen = bank in state.banks
            val available = chosen || state.banks.size < MAX_BANKS
            BankRow(
                bank = bank,
                chosen = chosen,
                onToggle = { actions.onBankToggle(bank) },
                modifier = if (available) Modifier else Modifier.alpha(UNAVAILABLE_ALPHA),
            )
        }
    }
}
