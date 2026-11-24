package com.cauri.feature.places.edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.PlaceRepository
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.pressScale
import com.cauri.core.design.component.rememberCauriHaptics
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.places.R
import kotlin.math.roundToInt

private val SheetRadius = 34.dp
private val ButtonHeight = 56.dp
private val SegmentHeight = 36.dp
private val ChipHeight = 36.dp
private val KnobSize = 24.dp
private val TrackHeight = 6.dp

// The radius moves by steps of 10 m.
private const val RADIUS_STEP = 10

/**
 * The form of a place, as the sheet of the mockup: the address to search, name, shop, home or work, the default
 * category and the reminder of a shop, the radius, which scroll; « Enregistrer le lieu » and, for a place saved
 * before, « Supprimer ce lieu » stay at the bottom of the screen.
 */
@Composable
internal fun PlaceForm(state: PlaceUiState, actions: PlaceActions, modifier: Modifier = Modifier) {
    GlassSurface(
        shape = RoundedCornerShape(topStart = SheetRadius, topEnd = SheetRadius),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            Fields(state = state, actions = actions, modifier = Modifier.weight(1f))
            Answers(state = state, actions = actions)
        }
    }
}

// The fields of the place, scrolling between the top of the form and its buttons.
@Composable
private fun Fields(state: PlaceUiState, actions: PlaceActions, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                start = CauriSpacing.xl,
                end = CauriSpacing.xl,
                top = CauriSpacing.s + 2.dp,
                bottom = CauriSpacing.l,
            ),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.l),
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 5.dp)
                .background(palette.handle, CircleShape),
        )
        Text(
            text = stringResource(if (state.editing) R.string.places_edit_title else R.string.places_new_title),
            style = MaterialTheme.typography.headlineSmall,
            color = palette.ink,
        )
        AddressSearch(state = state, actions = actions)
        Field(label = stringResource(R.string.places_name)) {
            CauriTextField(
                value = state.name,
                onValueChange = actions::onName,
                missing = false,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                placeholder = stringResource(R.string.places_name_hint),
            )
        }
        KindSegments(selected = state.kind, onSelect = actions::onKind)
        if (state.kind == PlaceKind.SHOP && state.categories.isNotEmpty()) {
            Field(label = stringResource(R.string.places_category)) { Categories(state = state, actions = actions) }
        }
        Radius(meters = state.radiusMeters, onChange = actions::onRadius)
        if (state.kind == PlaceKind.SHOP) {
            Remind(remind = state.remindAfterVisit, onChange = actions::onRemindAfterVisit)
        }
    }
}

// « Me rappeler après une visite » and its switch.
@Composable
private fun Remind(remind: Boolean, onChange: (Boolean) -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.places_remind),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = palette.ink,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = remind,
            onCheckedChange = {
                haptics.select()
                onChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = palette.accent,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = palette.chip,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

// « Enregistrer le lieu », then « Supprimer ce lieu » for a place saved before, at the bottom of the screen.
@Composable
private fun Answers(state: PlaceUiState, actions: PlaceActions) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs),
    ) {
        Button(
            onClick = {
                haptics.confirm()
                actions.onSave()
            },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(
                text = stringResource(R.string.places_save),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        if (state.editing) {
            TextButton(
                onClick = {
                    haptics.reject()
                    actions.onAskDelete(open = true)
                },
            ) {
                Text(
                    text = stringResource(R.string.places_delete),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.coralText,
                )
            }
        }
    }
}

/** A field of the form under its [label]. */
@Composable
internal fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s - 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = CauriTheme.palette.muted)
        content()
    }
}

// « Commerce », « Maison » and « Travail ».
@Composable
private fun KindSegments(selected: PlaceKind, onSelect: (PlaceKind) -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Row(modifier = Modifier.fillMaxWidth().background(palette.chip, CircleShape).padding(4.dp)) {
        PlaceKind.entries.forEach { kind ->
            val chosen = kind == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(SegmentHeight)
                    .clip(CircleShape)
                    .background(if (chosen) palette.segment else Color.Transparent)
                    .selectable(selected = chosen, role = Role.Tab, onClick = {
                        if (!chosen) haptics.select()
                        onSelect(kind)
                    }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        when (kind) {
                            PlaceKind.SHOP -> R.string.places_kind_shop
                            PlaceKind.HOME -> R.string.places_kind_home
                            PlaceKind.WORK -> R.string.places_kind_work
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (chosen) palette.ink else palette.muted,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Categories(state: PlaceUiState, actions: PlaceActions) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    val motion = CauriTheme.motion
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.s),
    ) {
        state.categories.forEach { category ->
            val selected = category.id == state.categoryId
            val fill by animateColorAsState(
                if (selected) palette.accentSoft else palette.chip,
                motion.tween(),
                label = "category fill",
            )
            val border by animateColorAsState(
                if (selected) palette.accent else palette.accent.copy(alpha = 0f),
                motion.tween(),
                label = "category border",
            )
            Box(
                modifier = Modifier
                    .heightIn(min = ChipHeight)
                    .pressScale()
                    .clip(CircleShape)
                    .background(fill, CircleShape)
                    .border(1.5.dp, border, CircleShape)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = {
                        haptics.select()
                        actions.onCategory(category.id)
                    })
                    .padding(horizontal = CauriSpacing.m + 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (selected) palette.accentText else palette.ink,
                )
            }
        }
    }
}

// « Rayon » and its value in metres, on a slider from the smallest radius to the largest.
@OptIn(ExperimentalMaterial3Api::class) // The slider's track is drawn from its state.
@Composable
private fun Radius(meters: Int, onChange: (Int) -> Unit) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    val min = PlaceRepository.MIN_RADIUS_METERS
    val max = PlaceRepository.MAX_RADIUS_METERS
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.xs)) {
        Row {
            Text(
                text = stringResource(R.string.places_radius_label),
                style = MaterialTheme.typography.labelMedium,
                color = palette.muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.places_meters, meters),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.ink,
            )
        }
        Slider(
            value = meters.toFloat(),
            onValueChange = {
                val step = it.roundToInt()
                if (step != meters) {
                    haptics.step()
                    onChange(step)
                }
            },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min) / RADIUS_STEP - 1,
            modifier = Modifier.fillMaxWidth(),
            // A white knob on a thin lagoon track, as in the mockup.
            thumb = {
                Box(
                    Modifier
                        .size(KnobSize)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(Color.White, CircleShape),
                )
            },
            track = { slider ->
                val range = slider.valueRange
                val share = (slider.value - range.start) / (range.endInclusive - range.start)
                Box(Modifier.fillMaxWidth().height(TrackHeight).background(palette.track, CircleShape)) {
                    Box(Modifier.fillMaxWidth(share).height(TrackHeight).background(palette.accent, CircleShape))
                }
            },
        )
    }
}
