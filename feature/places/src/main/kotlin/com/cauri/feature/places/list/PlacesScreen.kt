package com.cauri.feature.places.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.capture.permission.LocationAccess
import com.cauri.core.data.model.Place
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.pressScale
import com.cauri.core.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriRadius
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.places.MapArea
import com.cauri.feature.places.MapPin
import com.cauri.feature.places.PlacesRoundButton
import com.cauri.feature.places.R

private val MapHeight = 150.dp
private val IconBoxSize = 40.dp

// The pins keep this share of the map free on each side, so that none is cut by its edge.
private const val MAP_MARGIN = 0.12f

// Where a lone place, or places side by side, stand on the map: its middle.
private const val MAP_MIDDLE = 0.5f

/**
 * « Mes lieux »: where the map goes, the shops with their default category and visits, then home and work, which
 * never bring a reminder. A tap opens a place; « + » adds one through [onOpenPlace] with `null`; « Oui, l'ajouter »
 * of a place Cauri asks about, [onAddSuggestion] with its id.
 */
@Composable
internal fun PlacesScreen(
    onBack: () -> Unit,
    onOpenPlace: (Long?) -> Unit,
    onAddSuggestion: (Long) -> Unit,
    viewModel: PlacesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    // Named apart from the actions they back, which share their names.
    val back = onBack
    val open = onOpenPlace
    val addSuggestion = onAddSuggestion
    val actions = remember(viewModel, context, back, open, addSuggestion) {
        object : PlacesActions {
            override fun onBack() = back()

            override fun onOpenPlace(id: Long?) = open(id)

            override fun onAllowLocation() = context.startActivity(viewModel.locationSettings())

            override fun onAddSuggestion(id: Long) = addSuggestion(id)

            override fun onIgnoreSuggestion(id: Long) = viewModel.onIgnoreSuggestion(id)
        }
    }
    PlacesContent(state = state, actions = actions)
}

@Composable
private fun PlacesContent(state: PlacesUiState, actions: PlacesActions) {
    // The header stays while the places scroll under it.
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(modifier = Modifier.padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.l)) {
            Header(shops = state.shops.size, onBack = actions::onBack, onAdd = { actions.onOpenPlace(null) })
        }
        Places(state = state, actions = actions)
    }
}

// Where the map goes, the place Cauri asks about, the notice of the location, then the shops and the places without
// reminder.
@Composable
private fun Places(state: PlacesUiState, actions: PlacesActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = CauriSpacing.l),
        verticalArrangement = Arrangement.spacedBy(CauriSpacing.m + 2.dp),
    ) {
        MapArea(
            modifier = Modifier
                .fillMaxWidth()
                .height(MapHeight)
                .clip(RoundedCornerShape(CauriRadius.card))
                .border(1.dp, CauriTheme.palette.glassBorder, RoundedCornerShape(CauriRadius.card)),
            pins = pinsOf(state.shops + state.quiet),
        )
        if (state.suggestions.isNotEmpty()) SuggestionCards(suggestions = state.suggestions, actions = actions)
        if (state.location != LocationAccess.ALWAYS) LocationNotice(onAllow = actions::onAllowLocation)
        if (state.loaded && state.shops.isEmpty() && state.quiet.isEmpty()) {
            Text(
                text = stringResource(R.string.places_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = CauriTheme.palette.muted,
                modifier = Modifier.padding(horizontal = CauriSpacing.xs),
            )
        }
        if (state.shops.isNotEmpty()) {
            Section(
                title = stringResource(R.string.places_shops),
                places = state.shops,
                state = state,
                onOpen = actions::onOpenPlace,
            )
        }
        if (state.quiet.isNotEmpty()) {
            Section(
                title = stringResource(R.string.places_quiet),
                places = state.quiet,
                state = state,
                onOpen = actions::onOpenPlace,
            )
        }
    }
}

// Back, « Mes lieux » with the number of shops, and « + ».
@Composable
private fun Header(shops: Int, onBack: () -> Unit, onAdd: () -> Unit) {
    val palette = CauriTheme.palette
    val add = stringResource(R.string.places_add)
    Row(horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m), verticalAlignment = Alignment.CenterVertically) {
        PlacesRoundButton(
            icon = DesignR.drawable.ic_back,
            description = stringResource(R.string.places_back),
            onClick = onBack,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = stringResource(R.string.places_title),
                style = MaterialTheme.typography.headlineSmall,
                color = palette.ink,
            )
            Text(
                text = pluralStringResource(R.plurals.places_shops_count, shops, shops),
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
        Box(
            modifier = Modifier
                .size(CauriSpacing.touchTarget)
                .pressScale()
                .clip(CircleShape)
                .background(palette.accent)
                .clickable(onClickLabel = add, role = Role.Button, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_nav_add),
                contentDescription = add,
                tint = palette.onAccent,
            )
        }
    }
}

// Without the location in the background, no visit is noticed: the system screen where it is allowed.
@Composable
private fun LocationNotice(onAllow: () -> Unit) {
    val palette = CauriTheme.palette
    GlassSurface(shape = RoundedCornerShape(CauriRadius.card), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CauriSpacing.l),
            verticalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        ) {
            Text(
                text = stringResource(R.string.places_location_needed),
                style = MaterialTheme.typography.bodyMedium,
                color = palette.ink,
            )
            Button(
                onClick = onAllow,
                modifier = Modifier.height(40.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            ) {
                Text(
                    text = stringResource(R.string.places_location_allow),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, places: List<Place>, state: PlacesUiState, onOpen: (Long?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CauriSpacing.s)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = CauriTheme.palette.muted,
            modifier = Modifier.padding(start = CauriSpacing.xs),
        )
        GlassSurface(shape = RoundedCornerShape(CauriRadius.group), modifier = Modifier.fillMaxWidth()) {
            Column {
                places.forEachIndexed { index, place ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = CauriSpacing.l + IconBoxSize + CauriSpacing.m),
                            color = CauriTheme.palette.chip,
                        )
                    }
                    PlaceRow(
                        place = place,
                        categoryName = place.defaultCategoryId?.let(state.categoryNames::get),
                        onOpen = onOpen,
                    )
                }
            }
        }
    }
}

// A shop in gold with its category and visits; home and work muted, with their neighbourhood and « Sans rappel ».
@Composable
private fun PlaceRow(place: Place, categoryName: String?, onOpen: (Long?) -> Unit) {
    val palette = CauriTheme.palette
    val shop = place.kind == PlaceKind.SHOP
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onOpen(place.id) }
            .padding(horizontal = CauriSpacing.l, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(CauriSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(IconBoxSize)
                .background(if (shop) palette.goldSoft else palette.chip, RoundedCornerShape(CauriRadius.icon)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    when (place.kind) {
                        PlaceKind.SHOP -> DesignR.drawable.ic_place
                        PlaceKind.HOME -> R.drawable.ic_home
                        PlaceKind.WORK -> R.drawable.ic_work
                    },
                ),
                contentDescription = null,
                tint = if (shop) palette.goldText else palette.muted,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = palette.ink,
            )
            Text(
                text = if (shop) {
                    listOfNotNull(
                        categoryName,
                        pluralStringResource(R.plurals.places_visits, place.visits, place.visits),
                    )
                        .joinToString(" · ")
                } else {
                    place.area ?: stringResource(R.string.places_radius, place.radiusMeters)
                },
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted,
            )
        }
        Trailing(remindAfterVisit = place.remindAfterVisit)
    }
}

// « Sans rappel » for a place that never brings one, the chevron otherwise.
@Composable
private fun Trailing(remindAfterVisit: Boolean) {
    val palette = CauriTheme.palette
    if (!remindAfterVisit) {
        Text(
            text = stringResource(R.string.places_no_reminder),
            style = MaterialTheme.typography.labelMedium,
            color = palette.muted,
            modifier = Modifier
                .background(palette.chip, RoundedCornerShape(10.dp))
                .padding(horizontal = CauriSpacing.s + 2.dp, vertical = CauriSpacing.xs),
        )
    } else {
        Icon(painter = painterResource(DesignR.drawable.ic_chevron), contentDescription = null, tint = palette.muted)
    }
}

// The places on the map, by their position against each other: north up, the westmost on the left.
private fun pinsOf(places: List<Place>): List<MapPin> {
    if (places.isEmpty()) return emptyList()
    val south = places.minOf { it.latitude }
    val north = places.maxOf { it.latitude }
    val west = places.minOf { it.longitude }
    val east = places.maxOf { it.longitude }
    fun share(value: Double, low: Double, high: Double) =
        if (high > low) ((value - low) / (high - low)).toFloat() else MAP_MIDDLE
    return places.map {
        MapPin(
            x = MAP_MARGIN + (1 - 2 * MAP_MARGIN) * share(it.longitude, west, east),
            y = MAP_MARGIN + (1 - 2 * MAP_MARGIN) * (1 - share(it.latitude, south, north)),
            highlighted = it.kind == PlaceKind.SHOP,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlacesContentPreview() {
    fun place(id: Long, name: String, kind: PlaceKind, visits: Int, latitude: Double, longitude: Double) = Place(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        radiusMeters = 80,
        kind = kind,
        defaultCategoryId = 1L.takeIf { kind == PlaceKind.SHOP },
        remindAfterVisit = kind == PlaceKind.SHOP,
        area = null,
        address = null,
        visits = visits,
    )
    CauriTheme {
        Box(Modifier.background(CauriTheme.palette.background)) {
            PlacesContent(
                state = PlacesUiState(
                    shops = listOf(
                        place(
                            id = 1,
                            name = "Boulangerie Saker",
                            kind = PlaceKind.SHOP,
                            visits = 12,
                            latitude = 3.87,
                            longitude = 11.51,
                        ),
                        place(
                            id = 2,
                            name = "Marché Mokolo",
                            kind = PlaceKind.SHOP,
                            visits = 9,
                            latitude = 3.89,
                            longitude = 11.50,
                        ),
                    ),
                    quiet = listOf(
                        place(
                            id = 3,
                            name = "Maison",
                            kind = PlaceKind.HOME,
                            visits = 0,
                            latitude = 3.84,
                            longitude = 11.54,
                        ),
                    ),
                    categoryNames = mapOf(1L to "Alimentation"),
                    location = LocationAccess.WHILE_IN_USE,
                    loaded = true,
                ),
                actions = PreviewPlacesActions,
            )
        }
    }
}

/** Actions that do nothing, for previews. */
private object PreviewPlacesActions : PlacesActions {
    override fun onBack() = Unit

    override fun onOpenPlace(id: Long?) = Unit

    override fun onAllowLocation() = Unit

    override fun onAddSuggestion(id: Long) = Unit

    override fun onIgnoreSuggestion(id: Long) = Unit
}
