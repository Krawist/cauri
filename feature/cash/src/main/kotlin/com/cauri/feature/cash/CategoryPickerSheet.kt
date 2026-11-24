package com.cauri.feature.cash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.Category
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme

private val ChipHeight = 44.dp

/** Categories of « + Autre dépense »: a tap adds an expense in that category. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CategoryPickerSheet(categories: List<Category>, actions: GapSplitActions) {
    val palette = CauriTheme.palette
    ModalBottomSheet(
        onDismissRequest = { actions.onPickCategory(open = false) },
        // The sheet is its own window: there is nothing of the screen to blur, so the sheet colour is opaque.
        containerColor = palette.sheet.compositeOver(palette.background),
        contentColor = palette.ink,
        scrimColor = palette.scrim,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.handle) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.xl),
        ) {
            Text(
                text = stringResource(R.string.cash_split_pick_category),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = CauriSpacing.l),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            ) {
                categories.forEach { category ->
                    GlassSurface(
                        shape = CircleShape,
                        modifier = Modifier.height(ChipHeight),
                        interaction = Modifier.clickable(role = Role.Button, onClick = {
                            actions.onAddExpense(category)
                        }),
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = palette.ink,
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = CauriSpacing.l),
                        )
                    }
                }
            }
        }
    }
}
