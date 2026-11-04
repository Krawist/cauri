package com.cauri.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.IncomeSplitProposal
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.component.Stepper
import com.cauri.core.design.component.backdrop
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.component.screenBackdropShapes
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassScreen
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ButtonHeight = 56.dp
private val BarHeight = 10.dp
private val BarGap = 3.dp

/**
 * « Répartir 150 000 F »: the savings first, their rate moved by 5 % with « − » and « + », then what the fixed costs
 * of the month not paid yet need, then the rest for the budgets, over a bar of the three. « Valider la répartition »
 * keeps it through [onSplit].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IncomeSplitSheet(
    proposal: IncomeSplitProposal,
    onSavingsRate: (step: Int) -> Unit,
    onSplit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = CauriTheme.palette
    val haptics = rememberCauriHaptics()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.home_split_title, formatFcfa(proposal.income.amount)),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = stringResource(R.string.home_split_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                    )
                }
                SplitBar(proposal = proposal)
                Parts(proposal = proposal, onSavingsRate = onSavingsRate)
                Button(
                    onClick = {
                        haptics.confirm()
                        onSplit()
                    },
                    modifier = Modifier.fillMaxWidth().height(ButtonHeight),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accent,
                        contentColor = palette.onAccent,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.home_split_confirm),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

// The three parts in one glass card: savings in gold, fixed costs, then the budgets.
@Composable
private fun Parts(proposal: IncomeSplitProposal, onSavingsRate: (step: Int) -> Unit) {
    val palette = CauriTheme.palette
    val split = proposal.split
    GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
        Column {
            Part(
                title = stringResource(R.string.home_split_savings),
                detail = stringResource(R.string.home_split_savings_detail),
                amount = split.savings,
                color = palette.goldText,
            ) {
                Stepper(
                    value = proposal.savingsRate,
                    canLower = proposal.savingsRate > 0,
                    canRaise = proposal.savingsRate < IncomeSplitViewModel.MAX_RATE,
                    onStep = onSavingsRate,
                    label = stringResource(R.string.home_split_rate, proposal.savingsRate),
                )
            }
            HorizontalDivider(color = palette.chip)
            Part(
                title = stringResource(R.string.home_split_fixed),
                detail = proposal.fixedCosts.takeIf { it.isNotEmpty() }?.joinToString { it.name }
                    ?: stringResource(R.string.home_split_fixed_none),
                amount = split.fixedCosts,
                color = palette.coralText,
            )
            HorizontalDivider(color = palette.chip)
            Part(
                title = stringResource(R.string.home_split_budgets),
                detail = stringResource(R.string.home_split_budgets_detail),
                amount = split.budgets,
                color = palette.greenText,
            )
        }
    }
}

// Its name in its colour over what it holds, its amount at the end, and the stepper of the savings under them.
@Composable
private fun Part(
    title: String,
    detail: String,
    amount: Long,
    color: Color,
    control: (@Composable () -> Unit)? = null,
) {
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = color,
                )
                Text(text = detail, style = MaterialTheme.typography.bodySmall, color = palette.muted)
            }
            // The amounts roll as the savings rate moves.
            RollingNumber(value = amount) { shown ->
                Text(
                    text = stringResource(R.string.home_amount, formatFcfa(shown)),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink,
                )
            }
        }
        control?.invoke()
    }
}

// The income as one bar, each part as wide as its share.
@Composable
private fun SplitBar(proposal: IncomeSplitProposal) {
    val palette = CauriTheme.palette
    val split = proposal.split
    Row(
        modifier = Modifier.fillMaxWidth().height(BarHeight).clip(CircleShape),
        horizontalArrangement = Arrangement.spacedBy(BarGap),
    ) {
        listOf(split.savings to palette.gold, split.fixedCosts to palette.coral, split.budgets to palette.greenText)
            .filter { (amount, _) -> amount > 0 }
            .forEach { (amount, color) ->
                Spacer(Modifier.weight(amount.toFloat()).height(BarHeight).background(color))
            }
    }
}
