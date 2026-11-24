package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.AccountKind
import com.cauri.core.design.component.CauriTextField
import io.github.krawist.design.component.backdrop
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.component.screenBackdropShapes
import io.github.krawist.design.glass.GlassScreen
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val IconBoxSize = 40.dp
private val IconSize = 22.dp
private val SelectedBorderWidth = 1.5.dp

/**
 * « Ajouter un compte », as the bank list of the onboarding: a glass screen of its own over the screens' shapes, the
 * search, then the mobile money services and the banks not followed yet, several ticked at once, and « Ajouter » at the
 * bottom. Back or a swipe down closes it through [onDismiss] without adding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAccountSheet(onDismiss: () -> Unit, viewModel: AddAccountViewModel = hiltViewModel()) {
    val haptics = rememberCauriHaptics()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = CauriTheme.palette
    val close = {
        viewModel.onClosed()
        onDismiss()
    }
    LaunchedEffect(state.added) { if (state.added) close() }
    ModalBottomSheet(
        onDismissRequest = close,
        // Open in full: « Ajouter » stays in sight under the list.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.background,
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        GlassScreen(background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) }) {
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                Header(query = state.query, onQuery = viewModel::onQuery)
                Choices(state = state, onToggle = viewModel::onToggle, modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        haptics.confirm()
                        viewModel.onAdd()
                    },
                    enabled = state.chosen.isNotEmpty(),
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
                        text = pluralStringResource(
                            R.plurals.home_add_account_done,
                            state.chosen.size,
                            state.chosen.size,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

// « Ajouter un compte » over the search.
@Composable
private fun Header(query: String, onQuery: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = CauriSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
    ) {
        Text(
            text = stringResource(R.string.home_add_account_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        CauriTextField(
            value = query,
            onValueChange = onQuery,
            missing = false,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = stringResource(R.string.home_add_account_search),
        )
    }
}

@Composable
private fun Choices(state: AddAccountUiState, onToggle: (AccountChoice) -> Unit, modifier: Modifier = Modifier) {
    if (state.choices.isEmpty()) {
        Text(
            text = stringResource(R.string.home_add_account_none),
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
        items(state.choices, key = AccountChoice::provider) { choice ->
            ChoiceRow(choice = choice, chosen = choice.provider in state.chosen, onToggle = { onToggle(choice) })
        }
    }
}

// A glass card as at the onboarding: lagoon fill and border once ticked, the icon, the name and what it is.
@Composable
private fun ChoiceRow(choice: AccountChoice, chosen: Boolean, onToggle: () -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.tile)
    GlassSurface(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (chosen) Modifier.border(SelectedBorderWidth, palette.accent, shape) else Modifier),
        interaction = Modifier.selectable(selected = chosen, role = Role.Checkbox, onClick = {
            haptics.select()
            onToggle()
        }),
    ) {
        if (chosen) Box(Modifier.matchParentSize().background(palette.accentSoft))
        Row(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(
                    IconBoxSize,
                ).background(palette.accentSoft, RoundedCornerShape(CauriRadius.icon)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        if (choice.kind == AccountKind.MOBILE_MONEY) R.drawable.ic_phone else R.drawable.ic_bank,
                    ),
                    contentDescription = null,
                    tint = palette.accentText,
                    modifier = Modifier.size(IconSize),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = choice.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (chosen) palette.accentText else palette.ink,
                )
                Text(
                    text = choice.description.ifEmpty {
                        stringResource(
                            if (choice.kind == AccountKind.MOBILE_MONEY) {
                                R.string.home_add_account_mobile
                            } else {
                                R.string.home_add_account_bank
                            },
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
    }
}
