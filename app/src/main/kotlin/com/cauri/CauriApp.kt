package com.cauri

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cauri.feature.cash.cashGraph
import com.cauri.feature.cash.navigateToCashCount
import com.cauri.feature.home.HomeGraph
import com.cauri.feature.home.homeGraph
import com.cauri.feature.onboarding.OnboardingGraph
import com.cauri.feature.onboarding.onboardingGraph
import com.cauri.feature.transactions.navigateToQuickEntry
import com.cauri.feature.transactions.transactionsGraph
import kotlinx.coroutines.delay

/** Root of the app: the launch screen, then the onboarding on first launch or the home screen. */
@Composable
internal fun CauriApp(uiState: MainUiState, onOnboardingCompleted: () -> Unit) {
    // Saved so that a configuration change (rotation, dark mode) does not show the launch screen again.
    var launchShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(LaunchDuration)
        launchShown = true
    }
    // The preferences load in a few milliseconds: the launch screen covers it.
    if (launchShown && uiState is MainUiState.Ready) {
        CauriNavHost(
            onboardingCompleted = uiState.onboardingCompleted,
            onOnboardingCompleted = onOnboardingCompleted,
        )
    } else {
        LaunchScreen()
    }
}

@Composable
private fun CauriNavHost(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    // Chosen once: a new start destination would rebuild the graph under the back stack when the onboarding completes.
    val startDestination = remember { if (onboardingCompleted) HomeGraph else OnboardingGraph }
    NavHost(navController = navController, startDestination = startDestination) {
        onboardingGraph(
            onCompleted = {
                onOnboardingCompleted()
                navController.navigate(HomeGraph) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
        )
        homeGraph(
            onCountCash = { navController.navigateToCashCount() },
            onAddExpense = { navController.navigateToQuickEntry() },
        )
        cashGraph(onBack = { navController.popBackStack() })
        transactionsGraph(onBack = { navController.popBackStack() })
    }
}
