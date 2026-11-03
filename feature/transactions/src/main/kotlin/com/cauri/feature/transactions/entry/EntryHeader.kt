package com.cauri.feature.transactions.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.transactions.R

private val CloseSize = 44.dp
private val SegmentHeight = 36.dp

/** The cross closing the sheet through [onClose], then « Dépense », « Revenu » and « Transfert » in the middle. */
@Composable
internal fun EntryHeader(type: QuickEntryType, onType: (QuickEntryType) -> Unit, onClose: () -> Unit) {
    val palette = CauriTheme.palette
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(CloseSize),
            colors = IconButtonDefaults.iconButtonColors(containerColor = palette.chip, contentColor = palette.ink),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.transactions_close),
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            TypeSegments(selected = type, onSelect = onType)
        }
        Spacer(Modifier.size(CloseSize))
    }
}

// « Dépense », « Revenu », « Transfert » on a grey track, the chosen one raised.
@Composable
private fun TypeSegments(selected: QuickEntryType, onSelect: (QuickEntryType) -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Row(modifier = Modifier.background(palette.chip, CircleShape).padding(4.dp)) {
        QuickEntryType.entries.forEach { type ->
            val chosen = type == selected
            Box(
                modifier = Modifier
                    .height(SegmentHeight)
                    .clip(CircleShape)
                    .background(if (chosen) palette.segment else Color.Transparent)
                    .selectable(selected = chosen, role = Role.Tab, onClick = {
                        if (!chosen) haptics.select()
                        onSelect(type)
                    })
                    .padding(horizontal = CauriSpacing.l),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(type.label()),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (chosen) palette.ink else palette.muted,
                )
            }
        }
    }
}

private fun QuickEntryType.label() = when (this) {
    QuickEntryType.EXPENSE -> R.string.transactions_type_expense
    QuickEntryType.INCOME -> R.string.transactions_type_income
    QuickEntryType.TRANSFER -> R.string.transactions_type_transfer
}
