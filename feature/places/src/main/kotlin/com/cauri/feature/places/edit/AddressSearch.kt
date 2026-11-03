package com.cauri.feature.places.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.CauriTextField
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.places.R

private val IconBoxSize = 36.dp

/**
 * « Adresse », above the name: the addresses found as the user types open above the field, over the map, each a row
 * with its pin, a tap putting the place there, for a place where the user does not stand.
 */
@Composable
internal fun AddressSearch(state: PlaceUiState, actions: PlaceActions) {
    val density = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }
    val gap = with(density) { CauriSpacing.s.roundToPx() }
    // The list is anchored to the label and the field together, so that it opens above both.
    Box(modifier = Modifier.onSizeChanged { width = with(density) { it.width.toDp() } }) {
        Field(label = stringResource(R.string.places_address)) {
            CauriTextField(
                value = state.query,
                onValueChange = actions::onQuery,
                missing = false,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Search,
                ),
                placeholder = stringResource(R.string.places_search_hint),
            )
        }
        if (state.query.isNotBlank() && (state.searching || state.searched)) {
            // Above the field: under it, the keyboard and the form's buttons would hide it.
            Popup(popupPositionProvider = AboveAnchor(gap = gap), properties = PopupProperties(focusable = false)) {
                Box(Modifier.width(width)) { Results(state = state, actions = actions) }
            }
        }
    }
}

// Puts the popup right above what it is anchored to, from its left edge, [gap] pixels apart.
private class AboveAnchor(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ) = IntOffset(x = anchorBounds.left, y = (anchorBounds.top - popupContentSize.height - gap).coerceAtLeast(0))
}

// The addresses found, or why there are none yet.
@Composable
private fun Results(state: PlaceUiState, actions: PlaceActions) {
    val palette = CauriTheme.palette
    val shape = RoundedCornerShape(CauriRadius.group)
    // A popup has a window of its own, with nothing behind it to blur: the sheets' fill instead of glass.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = shape, ambientColor = palette.shadow, spotColor = palette.shadow)
            .background(palette.sheet.compositeOver(palette.background), shape)
            .border(1.dp, palette.glassBorder, shape),
    ) {
        Column {
            when {
                state.searching -> Note(stringResource(R.string.places_search_searching))
                state.results.isEmpty() -> Note(stringResource(R.string.places_search_none))
                else -> state.results.forEachIndexed { index, address ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = CauriSpacing.l + IconBoxSize + CauriSpacing.m),
                            color = palette.chip,
                        )
                    }
                    Result(address = address, onChoose = actions::onChooseAddress)
                }
            }
        }
    }
}

// Its pin, the name of the place in bold (else its first part, such as the street) and always its address under it.
@Composable
private fun Result(address: FoundAddress, onChoose: (FoundAddress) -> Unit) {
    val haptics = rememberCauriHaptics()
    val focus = LocalFocusManager.current
    val palette = CauriTheme.palette
    val title = address.name ?: address.label.substringBefore(", ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) {
                haptics.select()
                focus.clearFocus()
                onChoose(address)
            }
            .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(IconBoxSize).background(palette.accentSoft, RoundedCornerShape(CauriRadius.icon)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_place),
                contentDescription = null,
                tint = palette.accentText,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = address.label,
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = CauriTheme.palette.muted,
        modifier = Modifier.padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
    )
}
