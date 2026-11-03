package com.cauri.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.AccountBalance
import com.cauri.core.design.format.formatFcfa
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/**
 * « Tes comptes »: the balance of each digital account, as its last message gave it. « Disponible aujourd'hui » joins
 * it with the budgets (CAU-46). Nothing shows before a message gave a balance.
 */
@Composable
internal fun BalancesCard(balances: List<AccountBalance>) {
    if (balances.isEmpty()) return
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.tile), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m + 2.dp),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs + 2.dp),
        ) {
            Text(
                text = stringResource(R.string.home_balances_label),
                style = MaterialTheme.typography.labelLarge,
                color = palette.muted,
            )
            balances.forEach { balance ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = balance.account.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(R.string.home_amount, formatFcfa(balance.amount)),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = palette.ink,
                    )
                }
            }
        }
    }
}
