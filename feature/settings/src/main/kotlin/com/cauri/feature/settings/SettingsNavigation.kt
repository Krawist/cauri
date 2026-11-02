package com.cauri.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
data object SettingsGraph

@Serializable
internal data object SettingsRoute

fun NavController.navigateToSettings() = navigate(SettingsGraph)

/** Settings: for now the status of each permission (CAU-10); accounts and the battery guide come with their tickets. */
fun NavGraphBuilder.settingsGraph(onBack: () -> Unit) {
    navigation<SettingsGraph>(startDestination = SettingsRoute) {
        composable<SettingsRoute> { SettingsScreen(onBack = onBack) }
    }
}
