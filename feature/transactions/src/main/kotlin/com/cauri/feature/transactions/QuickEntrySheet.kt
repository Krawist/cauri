package com.cauri.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

/** Quick entry sheet, opened by « + » (CAU-26). Back or a swipe down closes it through [onDismiss]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntrySheet(onDismiss: () -> Unit) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // The sheet is its own window: there is nothing of the screen to blur, so the sheet colour is opaque.
        containerColor = palette.sheet.compositeOver(palette.background),
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        ) {
            Text(
                text = stringResource(R.string.transactions_quick_entry_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.transactions_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
        }
    }
}
