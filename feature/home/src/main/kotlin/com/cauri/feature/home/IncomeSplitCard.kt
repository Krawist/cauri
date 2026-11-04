package com.cauri.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.data.model.PendingIncome
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import java.time.Instant

private val ButtonHeight = 44.dp

/** Offers to split the newest income arrived, savings first, and opens its split; shows nothing without one. */
@Composable
internal fun IncomeSplit(viewModel: IncomeSplitViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state.income?.let { income ->
        IncomeSplitCard(income = income, onSplit = viewModel::onOpen, onDismiss = viewModel::onDismiss)
    }
    state.proposal?.let { proposal ->
        IncomeSplitSheet(
            proposal = proposal,
            onSavingsRate = viewModel::onSavingsRate,
            onSplit = viewModel::onSplit,
            onDismiss = viewModel::onClose,
        )
    }
}

// « Revenu reçu », « 150 000 F sur Orange Money », why split it now, then the answers.
@Composable
private fun IncomeSplitCard(income: PendingIncome, onSplit: () -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.hero), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_income),
                    contentDescription = null,
                    tint = palette.greenText,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.home_income_label),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.greenText,
                )
            }
            Text(
                text = stringResource(R.string.home_income_title, formatFcfa(income.amount), income.accountName),
                style = MaterialTheme.typography.titleLarge,
                color = palette.ink,
            )
            Text(
                text = stringResource(R.string.home_income_text),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Answers(onSplit = onSplit, onDismiss = onDismiss)
        }
    }
}

// « Répartir » and « Pas cette fois ».
@Composable
private fun Answers(onSplit: () -> Unit, onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onSplit,
            modifier = Modifier.height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(text = stringResource(R.string.home_income_split), style = MaterialTheme.typography.labelLarge)
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.height(ButtonHeight),
            colors = ButtonDefaults.textButtonColors(contentColor = palette.muted),
        ) {
            Text(text = stringResource(R.string.home_income_skip), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview
@Composable
private fun IncomeSplitCardPreview() {
    CauriTheme {
        IncomeSplitCard(
            income = PendingIncome(
                transactionId = 1,
                amount = 150_000,
                accountName = "Orange Money",
                counterparty = null,
                occurredAt = Instant.now(),
            ),
            onSplit = {},
            onDismiss = {},
        )
    }
}
