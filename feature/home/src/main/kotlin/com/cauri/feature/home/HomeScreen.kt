package com.cauri.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cauri.core.design.theme.CauriTheme

/** Temporary home screen; the real one, cash first, comes with CAU-25. */
@Composable
internal fun HomeScreen(onCountCash: () -> Unit, onAddExpense: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
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

@Preview
@Composable
private fun HomeScreenPreview() {
    CauriTheme { HomeScreen(onCountCash = {}, onAddExpense = {}) }
}
