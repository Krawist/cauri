package com.cauri.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cauri.core.data.model.CashBalance
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.RollingNumber
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.format.formatMoment
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val CountButtonHeight = 40.dp
private val CurrencySize = 26.sp

/**
 * « Cash dans ta poche »: the estimated balance with « ≈ », when it was last counted and how many cash expenses came
 * since, and « Compter mon cash » next to them, or under them on a narrow screen.
 *
 * @param cash Estimated cash; `null` before the first count, when only the button shows.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CashCard(cash: CashBalance?, onCountCash: () -> Unit, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.hero), modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cash),
                    contentDescription = null,
                    tint = palette.goldText,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.home_cash_label),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.goldText,
                )
            }
            // The button sits next to the amount when the screen is wide enough, under it otherwise.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                if (cash != null) {
                    Column(
                        modifier = Modifier.padding(end = CauriSpacing.m),
                        verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp),
                    ) {
                        CashAmount(amount = cash.amount)
                        Text(
                            text = countedLine(cash),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.muted,
                        )
                    }
                }
                CountButton(onClick = onCountCash)
            }
        }
    }
}

// « ≈ 7 500 F »: the sign and the currency smaller and muted, so the amount stands out.
@Composable
private fun CashAmount(amount: Long) {
    val palette = CauriTheme.palette
    val currency = stringResource(R.string.home_currency)
    // It rolls to its new value after an expense, a withdrawal or a count.
    RollingNumber(value = amount) { shown ->
        val text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = palette.muted)) { append("≈ ") }
            append(formatFcfa(shown))
            withStyle(SpanStyle(fontSize = CurrencySize, fontWeight = FontWeight.SemiBold, color = palette.muted)) {
                append(" ")
                append(currency)
            }
        }
        Text(text = text, style = MaterialTheme.typography.displayLarge, color = palette.ink)
    }
}

// « Compté hier à 20 h · 3 dépenses depuis »: the expenses only once there are some.
@Composable
private fun countedLine(cash: CashBalance): String {
    val counted = stringResource(R.string.home_counted, formatMoment(cash.countedAt))
    if (cash.expensesSinceCount == 0) return counted
    val expenses = pluralStringResource(R.plurals.home_expenses_since, cash.expensesSinceCount, cash.expensesSinceCount)
    return stringResource(R.string.home_counted_and_expenses, counted, expenses)
}

@Composable
private fun CountButton(onClick: () -> Unit) {
    val palette = CauriTheme.palette
    Button(
        onClick = onClick,
        modifier = Modifier.height(CountButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = palette.goldSoft, contentColor = palette.goldText),
        contentPadding = PaddingValues(horizontal = CauriSpacing.l),
    ) {
        Icon(painter = painterResource(DesignR.drawable.ic_count), contentDescription = null)
        Text(
            text = stringResource(R.string.home_count_cash),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(start = CauriSpacing.s),
        )
    }
}
