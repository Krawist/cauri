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
 */
@Serializable
internal data class PlaceRoute(val placeId: Long? = null)

fun NavController.navigateToPlaces() = navigate(PlacesGraph)

/** « Mes lieux » and the screen of a place (CAU-33). [onBack] leaves the places. */
fun NavGraphBuilder.placesGraph(navController: NavController, onBack: () -> Unit) {
    navigation<PlacesGraph>(startDestination = PlacesRoute) {
        composable<PlacesRoute> {
            PlacesScreen(
                onBack = onBack,
                onOpenPlace = { navController.navigate(PlaceRoute(placeId = it)) },
            )
        }
        composable<PlaceRoute> { PlaceScreen(onClose = { navController.popBackStack() }) }
    }
}
