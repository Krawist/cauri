package com.cauri.feature.insights

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.cauri.core.data.model.BudgetSuggestion
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.theme.CauriTheme

// Up to 10 000 000 F a month, as the amounts typed elsewhere.
private const val MAX_AMOUNT = 10_000_000L
private const val MAX_AMOUNT_DIGITS = 8

/** « Budget Alimentation »: the amount a month, the one suggested to start from; an empty or nil amount is refused. */
@Composable
internal fun BudgetAmountDialog(suggestion: BudgetSuggestion, onAmount: (Long) -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    val haptics = rememberCauriHaptics()
    var digits by rememberSaveable(suggestion.categoryId) { mutableStateOf(suggestion.amount.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.background,
        title = { Text(text = stringResource(R.string.insights_budget_of, suggestion.categoryName)) },
        text = {
            CauriTextField(
                value = digits,
                onValueChange = { digits = it.filter(Char::isDigit).take(MAX_AMOUNT_DIGITS) },
                missing = false,
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = FcfaVisualTransformation,
                autoFocus = true,
                trailing = { Text(text = stringResource(R.string.insights_per_month), color = palette.muted) },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    digits.toLongOrNull()?.takeIf { it in 1..MAX_AMOUNT }?.let {
                        haptics.confirm()
                        onAmount(it)
                    } ?: haptics.reject()
                },
            ) { Text(text = stringResource(R.string.insights_budget_save), color = palette.accentText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.insights_budget_cancel), color = palette.muted)
            }
        },
    )
}
