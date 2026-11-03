package com.cauri.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Insights
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val BarHeight = 12.dp
private val BarGap = 3.dp
private val LegendDot = 8.dp
private const val PERCENT = 100

/**
 * « 61 % de tes dépenses en cash » and the total, then one bar: the cash identified in gold, the rest of the counts no
 * expense explained in pale gold, the digital in lagoon, each with its legend.
 */
@Composable
internal fun CashShareCard(insights: Insights) {
    val palette = CauriTheme.palette
    val identified = insights.cash - insights.unidentified
    val digital = insights.spent - insights.cash
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.l + 2.dp),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s + 2.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.insights_cash_share, insights.cash * PERCENT / insights.spent),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.insights_amount, formatFcfa(insights.spent)),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(BarHeight),
                horizontalArrangement = Arrangement.spacedBy(BarGap),
            ) {
                listOf(identified to palette.gold, insights.unidentified to palette.goldSoft, digital to palette.accent)
                    .filter { it.first > 0 }
                    .forEach { (amount, color) ->
                        Box(
                            modifier = Modifier
                                .weight(amount.toFloat())
                                .height(BarHeight)
                                .background(color, RoundedCornerShape(BarHeight / 2)),
                        )
                    }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp)) {
                Legend(color = palette.gold, text = stringResource(R.string.insights_cash_identified))
                if (insights.unidentified > 0) {
                    Legend(
                        color = palette.goldSoft,
                        text = stringResource(R.string.insights_cash_unidentified, formatFcfa(insights.unidentified)),
                    )
                }
                Legend(color = palette.accent, text = stringResource(R.string.insights_digital))
            }
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(LegendDot).background(color, RoundedCornerShape(2.dp)))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = CauriTheme.palette.muted)
    }
}
