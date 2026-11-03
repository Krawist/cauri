package com.cauri.feature.transactions.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.EditableTransaction
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.format.FcfaVisualTransformation
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R
import com.cauri.feature.transactions.entry.QuickEntryViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

private val AmountHeight = 60.dp

/**
 * « Montant », typed in FCFA; a payment in another currency shows what it was, « 12,50 USD », until the user types
 * what it cost.
 */
@Composable
internal fun EditAmount(state: EditUiState, actions: EditActions) {
    val haptics = rememberCauriHaptics()
    // A digit refused over the largest amount is felt, not only read.
    LaunchedEffect(state.amountTooHigh) { if (state.amountTooHigh) haptics.reject() }
    val palette = CauriTheme.palette
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp)) {
        Label(text = stringResource(R.string.transactions_edit_amount))
        CauriTextField(
            value = state.amount,
            onValueChange = actions::onAmount,
            missing = false,
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            height = AmountHeight,
            visualTransformation = FcfaVisualTransformation,
            placeholder = state.transaction?.let { foreignAmount(it) },
            trailing = {
                Text(
                    text = stringResource(R.string.transactions_currency),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.muted,
                )
            },
        )
        if (state.amountTooHigh) {
            Text(
                text = stringResource(
                    R.string.transactions_amount_too_high,
                    formatFcfa(QuickEntryViewModel.MAX_AMOUNT),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = palette.coralText,
            )
        }
    }
}

/** A field's name, muted, above it. */
@Composable
internal fun Label(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = CauriTheme.palette.muted,
    )
}

// « 12,50 USD »; `null` for an operation in the account's currency.
@Composable
private fun foreignAmount(transaction: EditableTransaction): String? {
    val foreign = transaction.foreignAmount
    val currency = transaction.foreignCurrency
    if (foreign == null || currency == null) return null
    val digits = Currency.getInstance(currency).defaultFractionDigits.coerceAtLeast(0)
    val format = NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).apply {
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
    return stringResource(
        R.string.transactions_foreign_amount,
        format.format(BigDecimal.valueOf(foreign, digits)),
        currency,
    )
}
