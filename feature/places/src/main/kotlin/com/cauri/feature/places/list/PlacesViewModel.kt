package com.cauri.feature.places.list

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cauri.core.capture.permission.PermissionService
import com.cauri.core.capture.place.PlaceWatcher
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.repository.CategoryRepository
import com.cauri.core.data.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * « Mes lieux »: the shops, then home and work. The screen calls [refresh] each time it comes back, as after the user
 * allowed the location in the system settings.
 */
@HiltViewModel
internal class PlacesViewModel @Inject constructor(
    places: PlaceRepository,
    categories: CategoryRepository,
    private val permissions: PermissionService,
    private val watcher: PlaceWatcher,
) : ViewModel() {
    private val state = MutableStateFlow(PlacesUiState(location = permissions.status().location))

    val uiState: StateFlow<PlacesUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(places.places, categories.categories) { places, categories ->
                val (shops, quiet) = places.partition { it.kind == PlaceKind.SHOP }
                state.update {
                    it.copy(
                        shops = shops,
                        quiet = quiet,
                        categoryNames = categories.associate { category -> category.id to category.name },
                        loaded = true,
                    )
                }
            }.collect {}
        }
    }

    /** Reads the location access again, and has the geofences registered again if it changed. */
    fun refresh() {
        val location = permissions.status().location
        if (location != state.value.location) watcher.refresh()
        state.update { it.copy(location = location) }
    }

    /** System screen of Cauri's permissions, where the location is allowed « all the time ». */
    fun locationSettings(): Intent = permissions.appSettings()
}
