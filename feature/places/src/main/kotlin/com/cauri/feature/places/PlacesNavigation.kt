package com.cauri.feature.places

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.cauri.feature.places.edit.PlaceScreen
import com.cauri.feature.places.list.PlacesScreen
import kotlinx.serialization.Serializable

@Serializable
data object PlacesGraph

@Serializable
internal data object PlacesRoute

/**
 * A place to add or change.
 *
 * @property placeId Place changed; `null` for a new one.
 * @property suggestionId Place Cauri asks about that the new place answers « Oui » to, at its position; `null` for
 *   none.
 * @property notified Whether the new place is the one the notification asked about last.
 */
@Serializable
internal data class PlaceRoute(val placeId: Long? = null, val suggestionId: Long? = null, val notified: Boolean = false)

fun NavController.navigateToPlaces() = navigate(PlacesGraph)

/** The screen of a new place at the place the notification asked about: its « Oui ». */
fun NavController.navigateToSuggestedPlace() = navigate(PlaceRoute(notified = true))

/** « Mes lieux » and the screen of a place (CAU-33, CAU-35). [onBack] leaves the places. */
fun NavGraphBuilder.placesGraph(navController: NavController, onBack: () -> Unit) {
    navigation<PlacesGraph>(startDestination = PlacesRoute) {
        composable<PlacesRoute> {
            PlacesScreen(
                onBack = onBack,
                onOpenPlace = { navController.navigate(PlaceRoute(placeId = it)) },
                onAddSuggestion = { navController.navigate(PlaceRoute(suggestionId = it)) },
            )
        }
        composable<PlaceRoute> { PlaceScreen(onClose = { navController.popBackStack() }) }
    }
}
