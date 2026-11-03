package com.cauri.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object HomeGraph

@Serializable
internal data object HomeRoute

/**
 * Home screen (CAU-25). The other features are opened through the callbacks, never directly: [onEditTransaction] opens
 * the edit sheet of a transaction to confirm.
 */
fun NavGraphBuilder.homeGraph(onCountCash: () -> Unit, onOpenSettings: () -> Unit, onEditTransaction: (Long) -> Unit) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onCountCash = onCountCash,
                onOpenSettings = onOpenSettings,
                onEditTransaction = onEditTransaction,
            )
        }
    }
}
