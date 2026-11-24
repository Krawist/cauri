package com.cauri.feature.settings.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.design.component.rememberCauriHaptics
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.settings.R
import java.time.LocalTime

private val ButtonHeight = 56.dp
private val ChoiceHeight = 44.dp
private val SelectedBorderWidth = 1.5.dp

// The evening times the user can choose, every half hour.
private val SummaryTimes = (0..6).map { LocalTime.of(19, 0).plusMinutes(30L * it) }

/**
 * « Récapitulatif du soir »: « Automatique », which follows when the user answers their cards, or a time of the evening
 * from 19:00 to 22:00. Saved at once.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SummaryTimeSheet(state: RemindersUiState, actions: RemindersActions) {
    val palette = CauriTheme.palette
    val chosen = state.settings.summaryTime
    ModalBottomSheet(
        onDismissRequest = { actions.onSummary(open = false) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        ) {
            Text(text = stringResource(R.string.reminders_summary), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.reminders_summary_why),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Choice(
                label = stringResource(R.string.reminders_summary_automatic),
                selected = chosen == null,
                onSelect = { actions.onSummaryTime(null) },
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            ) {
                SummaryTimes.forEach { time ->
                    Choice(
                        label = stringResource(R.string.reminders_time, time.hour, time.minute),
                        selected = chosen == time,
                        onSelect = { actions.onSummaryTime(time) },
                    )
                }
            }
            Button(
                onClick = { actions.onSummary(open = false) },
                modifier = Modifier.fillMaxWidth().padding(top = CauriSpacing.s).height(ButtonHeight),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(text = stringResource(R.string.settings_done), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// A glass choice, lagoon once selected, as the other sheets of the settings.
@Composable
private fun Choice(label: String, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    GlassSurface(
        shape = CircleShape,
        modifier = modifier
            .height(ChoiceHeight)
            .then(if (selected) Modifier.border(SelectedBorderWidth, palette.accent, CircleShape) else Modifier),
        interaction = Modifier.selectable(selected = selected, role = Role.RadioButton) {
            haptics.select()
            onSelect()
        },
    ) {
        if (selected) Box(Modifier.matchParentSize().background(palette.accentSoft))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) palette.accentText else palette.ink,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = CauriSpacing.l),
        )
    }
}
