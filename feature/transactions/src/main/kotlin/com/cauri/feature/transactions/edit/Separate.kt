package com.cauri.feature.transactions.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

/**
 * What Cauri joined, a merge with an entry the user typed or two messages linked into a transfer, and « Séparer »,
 * which puts both operations back as they were; nothing for any other transaction.
 */
@Composable
internal fun Separate(state: EditUiState, actions: EditActions) {
    val transaction = state.transaction ?: return
    val joined = when {
        transaction.mergeId != null -> R.string.transactions_edit_merged
        transaction.transferLinkId != null -> R.string.transactions_edit_linked
        else -> return
    }
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CauriRadius.icon))
            .background(palette.chip)
            .padding(start = CauriSpacing.l, end = CauriSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(joined),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted,
            modifier = Modifier.weight(1f).padding(vertical = CauriSpacing.m),
        )
        TextButton(
            onClick = {
                haptics.confirm()
                actions.onSeparate()
            },
        ) {
            Text(
                text = stringResource(R.string.transactions_edit_separate),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.accent,
            )
        }
    }
}
