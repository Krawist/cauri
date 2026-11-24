package com.cauri.feature.cash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cauri.core.design.format.formatFcfa
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val CurrencySize = 18.sp

/**
 * « Total compté » and, once Cauri has an estimate, « Attendu ≈ » with the gap: coral when money is missing, green
 * otherwise.
 */
@Composable
internal fun CountSummary(total: Long, expected: Long?, gap: Long?) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.l + 2.dp, vertical = CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.cash_count_total),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = palette.muted,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = buildAnnotatedString {
                        append(formatFcfa(total))
                        withStyle(
                            SpanStyle(fontSize = CurrencySize, fontWeight = FontWeight.SemiBold, color = palette.muted),
                        ) {
                            append(" ")
                            append(stringResource(R.string.cash_currency))
                        }
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = palette.ink,
                )
            }
            if (expected != null && gap != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.cash_count_expected, formatFcfa(expected)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.muted,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = when {
                            gap > 0 -> stringResource(R.string.cash_count_missing, formatFcfa(gap))
                            gap < 0 -> stringResource(R.string.cash_count_surplus, formatFcfa(-gap))
                            else -> stringResource(R.string.cash_count_right)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (gap > 0) palette.coralText else palette.greenText,
                    )
                }
            }
        }
    }
}
