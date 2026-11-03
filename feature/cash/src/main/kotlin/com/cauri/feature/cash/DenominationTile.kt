package com.cauri.feature.cash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val TileHeight = 64.dp
private val StepButtonSize = 40.dp
private val CountedBorderWidth = 1.5.dp
private val StepSignSize = 20.sp

/**
 * A banknote or coin with its count, « − » and « + ». Gold once counted, as cash is. Banknotes are rounded
 * rectangles, coins pills.
 */
@Composable
internal fun DenominationTile(
    denomination: Denomination,
    count: Int,
    actions: CashCountActions,
    modifier: Modifier = Modifier,
) {
    val palette = CauriTheme.palette
    val shape = if (denomination.coin) CircleShape else RoundedCornerShape(CauriRadius.control)
    val counted = count > 0
    GlassSurface(
        shape = shape,
        modifier = modifier
            .height(TileHeight)
            .then(if (counted) Modifier.border(CountedBorderWidth, palette.gold, shape) else Modifier),
    ) {
        if (counted) Box(Modifier.matchParentSize().background(palette.goldSoft))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(start = CauriSpacing.m + 2.dp, end = CauriSpacing.s + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = formatFcfa(denomination.value),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.ink,
                )
                Text(
                    text = if (counted) {
                        stringResource(
                            R.string.cash_count_times,
                            count,
                        )
                    } else {
                        stringResource(R.string.cash_count_none)
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (counted) palette.goldText else palette.muted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp)) {
                StepButton(
                    sign = "−",
                    description = stringResource(R.string.cash_count_remove, formatFcfa(denomination.value)),
                    fill = palette.chip,
                    color = palette.ink,
                    onClick = { actions.onRemove(denomination) },
                )
                StepButton(
                    sign = "+",
                    description = stringResource(R.string.cash_count_add, formatFcfa(denomination.value)),
                    fill = palette.accent,
                    color = palette.onAccent,
                    onClick = { actions.onAdd(denomination) },
                )
            }
        }
    }
}

@Composable
private fun StepButton(sign: String, description: String, fill: Color, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(StepButtonSize)
            .clip(CircleShape)
            .background(fill)
            .clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = sign, fontSize = StepSignSize, fontWeight = FontWeight.Medium, color = color)
    }
}
