package com.cauri

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cauri.core.design.R as DesignR
import com.cauri.core.design.component.BottomBarTab
import com.cauri.core.design.component.CauriBottomBar
import com.cauri.core.design.component.backdrop
import com.cauri.core.design.component.screenBackdropShapes
import com.cauri.core.design.glass.GlassScreen
import com.cauri.feature.cash.cashGraph
import com.cauri.feature.cash.navigateToCashCount
import com.cauri.feature.home.HomeGraph
import com.cauri.feature.home.homeGraph
import com.cauri.feature.onboarding.OnboardingGraph
import com.cauri.feature.onboarding.onboardingGraph
import com.cauri.feature.settings.navigateToSettings
import com.cauri.feature.settings.settingsGraph
import com.cauri.feature.transactions.ActivityGraph
import com.cauri.feature.transactions.QuickEntrySheet
import com.cauri.feature.transactions.activityGraph
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

// The tabs of the bottom bar, in order, by the route of their graph.
private val TabRoutes = listOf(HomeGraph, ActivityGraph, GoalsRoute, InsightsRoute)

@Composable
private fun CauriNavHost(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    // Chosen once: a new start destination would rebuild the graph under the back stack when the onboarding completes.
    val startDestination = remember { if (onboardingCompleted) HomeGraph else OnboardingGraph }
    var quickEntryOpen by rememberSaveable { mutableStateOf(false) }
    val destination = navController.currentBackStackEntryAsState().value?.destination
    val selectedTab = TabRoutes.indexOfFirst { route ->
        destination?.hierarchy?.any { it.hasRoute(route::class) } == true
    }
    GlassScreen(
        // Drawn once behind every screen, which are transparent: glass cards and the bar blur these shapes.
        background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) },
        overlay = {
            // Only on the tabs: the bar is hidden during the onboarding and on screens opened from a tab.
            if (selectedTab >= 0) {
                TabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index -> navController.navigateToTab(TabRoutes[index]) },
                    onAdd = { quickEntryOpen = true },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        },
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            onboardingGraph(
                navController = navController,
                onCompleted = {
                    onOnboardingCompleted()
                    navController.navigate(HomeGraph) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
            homeGraph(
                onCountCash = { navController.navigateToCashCount() },
                onAddExpense = { quickEntryOpen = true },
                onOpenSettings = { navController.navigateToSettings() },
            )
            activityGraph()
            placeholderTabs()
            cashGraph(navController = navController, onBack = { navController.popBackStack() })
            settingsGraph(onBack = { navController.popBackStack() })
        }
    }
    if (quickEntryOpen) {
        QuickEntrySheet(onDismiss = { quickEntryOpen = false })
    }
}

@Composable
private fun TabBar(selectedTab: Int, onTabSelected: (Int) -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    CauriBottomBar(
        tabs = listOf(
            BottomBarTab(icon = DesignR.drawable.ic_nav_home, label = stringResource(R.string.tab_home)),
            BottomBarTab(
                icon = DesignR.drawable.ic_nav_activity,
                label = stringResource(R.string.tab_activity),
            ),
            BottomBarTab(icon = DesignR.drawable.ic_nav_goals, label = stringResource(R.string.tab_goals)),
            BottomBarTab(
                icon = DesignR.drawable.ic_nav_insights,
                label = stringResource(R.string.tab_insights),
            ),
        ),
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        addLabel = stringResource(R.string.add_expense),
        onAdd = onAdd,
        modifier = modifier,
    )
}

// Accueil stays at the bottom of the back stack: back from another tab returns to it, then leaves the app. Each tab
// keeps its own state when the user switches between them. It pops up to Accueil's screen, not to its graph entry:
// popping to the graph would remove the screen and leave nothing to go back to.
private fun NavHostController.navigateToTab(route: Any) {
    val homeScreen = (graph.findNode<HomeGraph>() as NavGraph).findStartDestination()
    navigate(route) {
        popUpTo(homeScreen.id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
