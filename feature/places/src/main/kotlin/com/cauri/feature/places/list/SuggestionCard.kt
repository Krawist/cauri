package com.cauri.feature.places.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cauri.core.data.model.PlaceSuggestion
import com.cauri.core.design.component.bleed
import com.cauri.core.design.component.rememberCauriHaptics
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.places.R
import com.cauri.feature.places.discovery.suggestionText

private val IconBoxSize = 40.dp
private val ButtonHeight = 40.dp

// Share of the width a card takes when another follows it, so that its edge shows.
private const val SHARE_WITH_NEXT = 0.9f

/**
 * The places Cauri asks about, one card each swiped sideways, the oldest first; the next one shows at the edge when
 * there are several.
 */
@Composable
internal fun SuggestionCards(suggestions: List<PlaceSuggestion>, actions: PlacesActions) {
    val list = rememberLazyListState()
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val width = if (suggestions.size > 1) maxWidth * SHARE_WITH_NEXT else maxWidth
        LazyRow(
            state = list,
            flingBehavior = rememberSnapFlingBehavior(list),
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
            contentPadding = PaddingValues(horizontal = CauriSpacing.xl),
            modifier = Modifier.bleed(CauriSpacing.xl),
        ) {
            items(suggestions, key = { it.id }) { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    actions = actions,
                    modifier = Modifier.width(width).animateItem(),
                )
            }
        }
    }
}

// « Tu viens souvent ici. C'est un commerce ? », outlined in lagoon as in the mockup, with its answers.
@Composable
private fun SuggestionCard(suggestion: PlaceSuggestion, actions: PlacesActions, modifier: Modifier = Modifier) {
    val palette = CauriTheme.palette
    val context = LocalContext.current
    val shape = RoundedCornerShape(CauriRadius.card)
    Column(
        modifier = modifier
            .background(palette.accentSoft, shape)
            .border(1.5.dp, palette.accent, shape)
            .padding(CauriSpacing.l),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(IconBoxSize).background(palette.accent, RoundedCornerShape(CauriRadius.icon)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sparkle),
                    contentDescription = null,
                    tint = palette.onAccent,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.places_suggestion_title),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                )
                Text(
                    text = suggestionText(context, suggestion),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted,
                )
            }
        }
        Answers(suggestionId = suggestion.id, actions = actions)
    }
}

// « Oui, l'ajouter » in lagoon, « Non, ignorer » in glass.
@Composable
private fun Answers(suggestionId: Long, actions: PlacesActions) {
    val haptics = rememberCauriHaptics()
    val palette = CauriTheme.palette
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Button(
            onClick = {
                haptics.confirm()
                actions.onAddSuggestion(suggestionId)
            },
            modifier = Modifier.weight(1f).height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
        ) {
            Text(
                text = stringResource(R.string.places_suggestion_add),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        Button(
            onClick = {
                haptics.reject()
                actions.onIgnoreSuggestion(suggestionId)
            },
            modifier = Modifier.weight(1f).height(ButtonHeight),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.glassStrong, contentColor = palette.ink),
        ) {
            Text(
                text = stringResource(R.string.places_suggestion_ignore),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}
