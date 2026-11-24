package com.cauri.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.PendingType
import io.github.krawist.design.component.backdrop
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.component.screenBackdropShapes
import io.github.krawist.design.glass.GlassScreen
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.edit.EditActions
import com.cauri.feature.transactions.edit.EditAmount
import com.cauri.feature.transactions.edit.EditCategories
import com.cauri.feature.transactions.edit.EditDetails
import com.cauri.feature.transactions.edit.EditHeader
import com.cauri.feature.transactions.edit.EditUiState
import com.cauri.feature.transactions.edit.EditViewModel
import com.cauri.feature.transactions.edit.Separate
import com.cauri.feature.transactions.edit.SplitParts

private val ButtonHeight = 56.dp
private const val CONFIRM_WEIGHT = 1.4f

/**
 * Edit sheet of the transaction [transactionId], opened by a tap on a card to confirm: amount, category (« Proposée par
 * Cauri »), account, date, note and the split across several categories, « Séparer » for what Cauri joined; « Rien
 * dépensé » and « Valider » at the bottom. Back or a swipe down closes it through [onDismiss] without answering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(transactionId: Long, onDismiss: () -> Unit) {
    val viewModel: EditViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = CauriTheme.palette
    LaunchedEffect(transactionId) { viewModel.onOpen(transactionId) }
    val close = {
        viewModel.onClose()
        onDismiss()
    }
    LaunchedEffect(state.closed) { if (state.closed) close() }
    ModalBottomSheet(
        onDismissRequest = close,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.background,
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        // A glass screen of its own over the screens' shapes, as the quick entry.
        GlassScreen(
            fillMaxSize = false,
            background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) },
        ) {
            if (state.transaction?.transactionId == transactionId) EditContent(state = state, actions = viewModel)
        }
    }
}

@Composable
private fun EditContent(state: EditUiState, actions: EditActions) {
    val transaction = state.transaction ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // A small screen scrolls rather than cutting « Valider ».
            .verticalScroll(rememberScrollState())
            .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.l + 2.dp),
    ) {
        EditHeader(transaction = transaction, accounts = state.accounts)
        EditAmount(state = state, actions = actions)
        EditCategories(state = state, actions = actions)
        EditDetails(state = state, actions = actions)
        SplitParts(state = state, actions = actions)
        Separate(state = state, actions = actions)
        Answers(state = state, actions = actions)
    }
}

// « Rien dépensé » (« Ignorer » for what is not an expense) while it waits for the user, and « Valider ».
@Composable
private fun Answers(state: EditUiState, actions: EditActions) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    val transaction = state.transaction ?: return
    Row(
        modifier = Modifier.padding(top = CauriSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m - 2.dp),
    ) {
        if (transaction.pending) {
            Button(
                onClick = {
                    haptics.reject()
                    actions.onDismiss()
                },
                modifier = Modifier.weight(1f).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.chip, contentColor = palette.coralText),
            ) {
                Text(
                    text = stringResource(
                        if (transaction.type == PendingType.EXPENSE) {
                            R.string.transactions_edit_nothing_spent
                        } else {
                            R.string.transactions_edit_ignore
                        },
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
        Button(
            onClick = {
                haptics.confirm()
                actions.onConfirm()
            },
            enabled = state.canConfirm,
            modifier = Modifier.weight(CONFIRM_WEIGHT).height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(
                text = stringResource(R.string.transactions_edit_confirm),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}
