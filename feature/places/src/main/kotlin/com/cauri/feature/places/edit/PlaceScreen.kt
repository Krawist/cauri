package com.cauri.feature.places.edit

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.data.model.Category
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.PlaceRepository
import io.github.krawist.design.glass.GlassSurface
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.places.MapArea
import com.cauri.feature.places.PlacesRoundButton
import com.cauri.feature.places.R

private val MapHeight = 330.dp

// The form starts over the bottom of the map, as the sheet of the mockup.
private val FormTop = 296.dp

// Share of the map's width the radius circle takes, from the smallest radius to the largest.
private const val SMALLEST_CIRCLE = 0.12f
private const val LARGEST_CIRCLE = 0.42f

/**
 * Screen of a place, new or saved before: where the map goes, with the radius around the pin, « Ma position », and the
 * form of the mockup « Ajouter un lieu ». A new place is located as it opens, the location asked first when needed;
 * one Cauri suggested opens at its own position.
 */
@Composable
internal fun PlaceScreen(onClose: () -> Unit, viewModel: PlaceViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val askLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.onLocate()
    }
    val locate = {
        val allowed = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (allowed) {
            viewModel.onLocate()
        } else {
            askLocation.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }
    // A place saved before, or suggested, has its position already.
    LaunchedEffect(Unit) { if (!state.editing && !state.suggested) locate() }
    LaunchedEffect(state.closed) { if (state.closed) onClose() }
    PlaceContent(state = state, actions = viewModel, onLocate = locate)
    if (state.confirmingDelete) DeleteDialog(state = state, actions = viewModel)
}

@Composable
private fun PlaceContent(state: PlaceUiState, actions: PlaceActions, onLocate: () -> Unit) {
    // Transparent, as the other screens: the shapes behind them show through the glass.
    Box(modifier = Modifier.fillMaxSize()) {
        MapArea(
            modifier = Modifier.fillMaxWidth().height(MapHeight),
            circle = circleOf(state.radiusMeters).takeIf { state.latitude != null },
        )
        PositionStatus(state = state, modifier = Modifier.align(Alignment.TopCenter).padding(top = FormTop - 52.dp))
        // The form stays under the map, down to the bottom of the screen; its fields scroll inside it.
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(FormTop))
            if (state.loaded) PlaceForm(state = state, actions = actions, modifier = Modifier.weight(1f))
        }
        TopButtons(locating = state.locating, onClose = actions::onClose, onLocate = onLocate)
    }
}

// The cross, and « Ma position » on the right.
@Composable
private fun TopButtons(locating: Boolean, onClose: () -> Unit, onLocate: () -> Unit) {
    val palette = CauriTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = CauriSpacing.xl, vertical = CauriSpacing.m),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PlacesRoundButton(
            icon = R.drawable.ic_close,
            description = stringResource(R.string.places_close),
            onClick = onClose,
        )
        GlassSurface(
            shape = CircleShape,
            modifier = Modifier.height(CauriSpacing.touchTarget),
            interaction = Modifier.clickable(enabled = !locating, role = Role.Button, onClick = onLocate),
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = CauriSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(CauriSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_locate),
                    contentDescription = null,
                    tint = palette.ink,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.places_my_position),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.ink,
                )
            }
        }
    }
}

// Where the position stands: being looked for, found and how precisely, saved before, or not found; with its
// neighbourhood once known.
@Composable
private fun PositionStatus(state: PlaceUiState, modifier: Modifier = Modifier) {
    val accuracy = state.accuracyMeters
    val area = state.area
    val text = when {
        state.locating -> stringResource(R.string.places_position_locating)
        state.notFound -> stringResource(R.string.places_position_not_found)
        accuracy != null && area != null -> stringResource(R.string.places_position_found_in, area, accuracy)
        accuracy != null -> stringResource(R.string.places_position_found, accuracy)
        area != null -> area
        state.latitude != null -> stringResource(R.string.places_position_saved)
        else -> stringResource(R.string.places_position_none)
    }
    GlassSurface(shape = CircleShape, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (state.notFound) CauriTheme.palette.coralText else CauriTheme.palette.ink,
            modifier = Modifier.padding(horizontal = CauriSpacing.m, vertical = CauriSpacing.s - 2.dp),
        )
    }
}

@Composable
private fun DeleteDialog(state: PlaceUiState, actions: PlaceActions) {
    val palette = CauriTheme.palette
    AlertDialog(
        onDismissRequest = { actions.onAskDelete(open = false) },
        containerColor = palette.background,
        title = { Text(text = stringResource(R.string.places_delete_title, state.name), color = palette.ink) },
        text = { Text(text = stringResource(R.string.places_delete_text), color = palette.muted) },
        confirmButton = {
            TextButton(onClick = actions::onDelete) {
                Text(text = stringResource(R.string.places_delete_confirm), color = palette.coralText)
            }
        },
        dismissButton = {
            TextButton(onClick = { actions.onAskDelete(open = false) }) {
                Text(text = stringResource(R.string.places_delete_cancel), color = palette.ink)
            }
        },
    )
}

// The circle grows with the radius, between the smallest and the largest share of the map.
private fun circleOf(radiusMeters: Int): Float {
    val span = PlaceRepository.MAX_RADIUS_METERS - PlaceRepository.MIN_RADIUS_METERS
    val share = (radiusMeters - PlaceRepository.MIN_RADIUS_METERS).toFloat() / span
    return SMALLEST_CIRCLE + (LARGEST_CIRCLE - SMALLEST_CIRCLE) * share
}

@Preview(name = "Light", heightDp = 844)
@Preview(name = "Dark", heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlaceContentPreview() {
    CauriTheme {
        PlaceContent(
            state = PlaceUiState(
                name = "Boutique du quartier",
                categories = listOf(
                    Category(id = 1, name = "Alimentation", nature = CategoryNature.EXPENSE),
                    Category(id = 2, name = "Snacks", nature = CategoryNature.EXPENSE),
                ),
                categoryId = 1,
                latitude = 3.86,
                longitude = 11.52,
                accuracyMeters = 12,
                canSave = true,
            ),
            actions = PreviewPlaceActions,
            onLocate = {},
        )
    }
}

/** Actions that do nothing, for previews. */
// One function per gesture of the screen.
@Suppress("TooManyFunctions")
private object PreviewPlaceActions : PlaceActions {
    override fun onClose() = Unit

    override fun onLocate() = Unit

    override fun onQuery(query: String) = Unit

    override fun onChooseAddress(address: FoundAddress) = Unit

    override fun onName(name: String) = Unit

    override fun onKind(kind: PlaceKind) = Unit

    override fun onCategory(id: Long) = Unit

    override fun onRadius(meters: Int) = Unit

    override fun onRemindAfterVisit(remind: Boolean) = Unit

    override fun onSave() = Unit

    override fun onAskDelete(open: Boolean) = Unit

    override fun onDelete() = Unit
}
