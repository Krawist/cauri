package com.cauri.feature.places.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.place.AddressService
import com.cauri.core.capture.place.FoundAddress
import com.cauri.core.capture.place.LocationService
import com.cauri.core.data.model.CategoryNature
import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.DiscoveryRepository
import com.cauri.core.data.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen of a place, new, suggested by Cauri, or saved before: its name, kind, default category, radius and reminder,
 * at the position where the user stands or at an address found by the search. A new place is located as soon as the
 * screen asks.
 */
// One function per gesture of the screen.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class PlaceViewModel @Inject constructor(
    savedState: SavedStateHandle,
    categories: CategoryRepository,
    private val places: PlaceRepository,
    private val location: LocationService,
    private val addresses: AddressService,
    private val discovery: DiscoveryRepository,
) : ViewModel(),
    PlaceActions {
    // The place changed; `null` for a new one.
    private val placeId: Long? = savedState["placeId"]

    // The place Cauri asks about, chosen in « Mes lieux », or the one its notification asked about last.
    private val chosenSuggestion: Long? = savedState["suggestionId"]
    private val notified: Boolean = savedState["notified"] ?: false
    private val suggested = chosenSuggestion != null || notified

    private val state =
        MutableStateFlow(PlaceUiState(editing = placeId != null, suggested = suggested, loaded = placeId == null))

    val uiState: StateFlow<PlaceUiState> = state.asStateFlow()

    private var search: Job? = null

    init {
        if (suggested) viewModelScope.launch { openSuggestion() }
        viewModelScope.launch {
            categories.categories.collect { all ->
                change { it.copy(categories = all.filter { category -> category.nature == CategoryNature.EXPENSE }) }
            }
        }
        if (placeId != null) {
            viewModelScope.launch {
                val place = places.getPlace(placeId) ?: return@launch change { it.copy(closed = true) }
                change {
                    it.copy(
                        loaded = true,
                        name = place.name,
                        kind = place.kind,
                        categoryId = place.defaultCategoryId,
                        radiusMeters = place.radiusMeters,
                        remindAfterVisit = place.remindAfterVisit,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        area = place.area,
                        address = place.address,
                        query = place.address.orEmpty(),
                    )
                }
            }
        }
    }

    // At the place suggested, its address looked for as for the phone's position.
    private suspend fun openSuggestion() {
        val suggestion = chosenSuggestion?.let { discovery.getSuggestion(it) } ?: discovery.getLastNotified() ?: return
        change {
            it.copy(
                suggestionId = suggestion.id,
                latitude = suggestion.latitude,
                longitude = suggestion.longitude,
                area = suggestion.area,
            )
        }
        val found = addresses.addressAt(latitude = suggestion.latitude, longitude = suggestion.longitude)
        if (found != null) change { it.at(found) }
    }

    override fun onClose() = change { it.copy(closed = true) }

    override fun onLocate() {
        if (state.value.locating) return
        change { it.copy(locating = true, notFound = false) }
        viewModelScope.launch {
            val position = location.currentPosition()
            change {
                if (position == null) {
                    it.copy(locating = false, notFound = true)
                } else {
                    it.copy(
                        locating = false,
                        latitude = position.latitude,
                        longitude = position.longitude,
                        accuracyMeters = position.accuracyMeters,
                        address = null,
                        area = null,
                        query = "",
                    )
                }
            }
            if (position != null) {
                val found = addresses.addressAt(latitude = position.latitude, longitude = position.longitude)
                change { if (found != null && it.latitude == position.latitude) it.at(found) else it }
            }
        }
    }

    override fun onQuery(query: String) {
        search?.cancel()
        change { it.copy(query = query, results = emptyList(), searching = false, searched = false) }
        val text = query.trim()
        if (text.length < MIN_QUERY_LENGTH) return
        search = viewModelScope.launch {
            // Waits for the user to stop typing, rather than asking for every letter.
            delay(SEARCH_DELAY_MILLIS)
            change { it.copy(searching = true) }
            val found = addresses.search(text)
            change { it.copy(results = found, searching = false, searched = true) }
        }
    }

    override fun onChooseAddress(address: FoundAddress) {
        search?.cancel()
        change {
            it.copy(latitude = address.latitude, longitude = address.longitude, accuracyMeters = null, notFound = false)
                .at(address)
        }
    }

    // The field shows the [address]; the name the map knows, when it knows one, names the place until the user does.
    private fun PlaceUiState.at(address: FoundAddress) = copy(
        address = address.label,
        area = address.area,
        query = address.label,
        name = name.ifBlank { address.name.orEmpty().take(MAX_NAME_LENGTH) },
        results = emptyList(),
        searching = false,
        searched = false,
    )

    override fun onName(name: String) = change { it.copy(name = name.take(MAX_NAME_LENGTH)) }

    override fun onKind(kind: PlaceKind) = change { it.copy(kind = kind) }

    override fun onCategory(id: Long) = change { it.copy(categoryId = id.takeIf { chosen -> chosen != it.categoryId }) }

    override fun onRadius(meters: Int) = change {
        it.copy(radiusMeters = meters.coerceIn(PlaceRepository.MIN_RADIUS_METERS, PlaceRepository.MAX_RADIUS_METERS))
    }

    override fun onRemindAfterVisit(remind: Boolean) = change { it.copy(remindAfterVisit = remind) }

    override fun onSave() {
        val edit = state.value
        val latitude = edit.latitude
        val longitude = edit.longitude
        if (!edit.canSave || latitude == null || longitude == null) return
        val place = NewPlace(
            name = edit.name,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = edit.radiusMeters,
            kind = edit.kind,
            // Home and work bring no expense to propose a category for.
            defaultCategoryId = edit.categoryId.takeIf { edit.kind == PlaceKind.SHOP },
            remindAfterVisit = edit.remindAfterVisit,
            area = edit.area,
            address = edit.address,
        )
        change { it.copy(closed = true) }
        viewModelScope.launch {
            if (placeId == null) places.addPlace(place) else places.updatePlace(id = placeId, place = place)
            edit.suggestionId?.let { discovery.answer(id = it, accepted = true) }
        }
    }

    override fun onAskDelete(open: Boolean) = change { it.copy(confirmingDelete = open) }

    override fun onDelete() {
        val id = placeId ?: return
        change { it.copy(confirmingDelete = false, closed = true) }
        viewModelScope.launch { places.deletePlace(id) }
    }

    // Updates the state and whether it can be saved.
    private fun change(transform: (PlaceUiState) -> PlaceUiState) = state.update { current ->
        transform(current).let {
            it.copy(canSave = it.loaded && it.name.isNotBlank() && it.latitude != null && it.longitude != null)
        }
    }

    private companion object {
        const val MAX_NAME_LENGTH = 40

        // Fewer letters find too many addresses.
        const val MIN_QUERY_LENGTH = 3
        const val SEARCH_DELAY_MILLIS = 400L
    }
}
