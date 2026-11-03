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
 * the edit sheet of a transaction to confirm, [onSplitBalanceGap] the split of the gap left by the balance typed for a
 * digital account (account, typed, shown, in FCFA).
 */
fun NavGraphBuilder.homeGraph(
    onCountCash: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onSplitBalanceGap: (accountId: Long, typed: Long, shown: Long) -> Unit,
) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onCountCash = onCountCash,
                onOpenSettings = onOpenSettings,
                onEditTransaction = onEditTransaction,
                onSplitBalanceGap = onSplitBalanceGap,
            )
        }
    }
}
