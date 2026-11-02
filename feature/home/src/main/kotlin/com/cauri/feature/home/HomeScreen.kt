package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.CashBalance
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriTheme
import java.time.Duration
import java.time.Instant

private val ProfileButtonSize = 52.dp
private val ProfileButtonElevation = 16.dp
private val ProfileRingInset = 4.dp
private val ProfileRingWidth = 1.5.dp

/** Temporary home screen; the real one, cash first, comes with CAU-25. */
@Composable
internal fun HomeScreen(
    onCountCash: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onCountCash = onCountCash,
        onAddExpense = onAddExpense,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onCountCash: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
                ProfileButton(initial = state.initial, onClick = onOpenSettings)
            }
            CashCard(cash = state.cash, onCountCash = onCountCash)
            OutlinedButton(onClick = onAddExpense) { Text(stringResource(R.string.home_add_expense)) }
        }
    }
}

// The user's initial on a raised glass circle, as in the mockup, with a lagoon ring so it reads as the way to the
// settings.
@Composable
private fun ProfileButton(initial: String, onClick: () -> Unit) {
    val palette = CauriTheme.palette
    val description = stringResource(R.string.home_settings)
    GlassSurface(
        shape = CircleShape,
        modifier = Modifier
            .size(ProfileButtonSize)
            .semantics { contentDescription = description },
        strong = true,
        elevation = ProfileButtonElevation,
        interaction = Modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(ProfileRingInset)
                .background(palette.accentSoft, CircleShape)
                .border(ProfileRingWidth, palette.accent, CircleShape),
        )
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.accentText,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    CauriTheme {
        HomeContent(
            state = HomeUiState(
                initial = "K",
                cash = CashBalance(
                    amount = 7_500,
                    countedAt = Instant.now().minus(Duration.ofHours(1)),
                    expensesSinceCount = 3,
                ),
            ),
            onCountCash = {},
            onAddExpense = {},
            onOpenSettings = {},
        )
    }
}
