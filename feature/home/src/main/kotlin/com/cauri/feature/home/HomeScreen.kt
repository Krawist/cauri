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
import androidx.compose.material3.Button
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
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriTheme

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
    val initial by viewModel.initial.collectAsStateWithLifecycle()
    HomeContent(
        initial = initial,
        onCountCash = onCountCash,
        onAddExpense = onAddExpense,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
private fun HomeContent(
    initial: String,
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
                ProfileButton(initial = initial, onClick = onOpenSettings)
            }
            Text(
                text = stringResource(R.string.home_cash_label),
                style = MaterialTheme.typography.labelMedium,
                color = CauriTheme.palette.goldText,
            )
            Button(onClick = onCountCash) { Text(stringResource(R.string.home_count_cash)) }
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
    CauriTheme { HomeContent(initial = "K", onCountCash = {}, onAddExpense = {}, onOpenSettings = {}) }
}
