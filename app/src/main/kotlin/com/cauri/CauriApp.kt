package com.cauri

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cauri.core.capture.security.DeviceSecurity
import com.cauri.core.design.R as DesignR
import io.github.krawist.design.component.AnimatedPresence
import com.cauri.core.design.component.BottomBarTab
import com.cauri.core.design.component.CauriBottomBar
import com.cauri.core.design.component.LocalHelpBubbles
import io.github.krawist.design.component.backdrop
import com.cauri.core.design.component.screenBackdropShapes
import io.github.krawist.design.glass.GlassScreen
import com.cauri.core.design.theme.CauriSpacing
import com.cauri.core.design.theme.CauriTheme
import com.cauri.feature.cash.cashGraph
import com.cauri.feature.cash.navigateToBalanceGap
import com.cauri.feature.cash.navigateToCashCount
import com.cauri.feature.goals.GoalsGraph
import com.cauri.feature.goals.goalsGraph
import com.cauri.feature.home.HomeGraph
import com.cauri.feature.home.homeGraph
import com.cauri.feature.home.navigateToEveningSummary
import com.cauri.feature.insights.InsightsGraph
import com.cauri.feature.insights.insightsGraph
import com.cauri.feature.onboarding.OnboardingGraph
import com.cauri.feature.onboarding.onboardingGraph
import com.cauri.feature.places.navigateToPlaces
import com.cauri.feature.places.navigateToSuggestedPlace
import com.cauri.feature.places.placesGraph
import com.cauri.feature.settings.navigateToRestore
import com.cauri.feature.settings.navigateToSettings
import com.cauri.feature.settings.settingsGraph
import com.cauri.feature.transactions.ActivityGraph
import com.cauri.feature.transactions.EditTransactionSheet
import com.cauri.feature.transactions.QuickEntryMergeBar
import com.cauri.feature.transactions.QuickEntrySheet
import com.cauri.feature.transactions.activityGraph
import kotlinx.coroutines.delay

/**
 * Root of the app: the launch screen, then the onboarding on first launch or the home screen.
 *
 * @param notificationTarget Screen a notification asked for; `null` otherwise. [onNotificationTargetShown] once it is
 *   shown.
 * @param lock Whether the app is locked by the phone's [security] (CAU-57): the lock screen covers it until
 *   [onUnlocked].
 */
// Parameters: the state of the app, what a notification asked for and the lock.
@Suppress("LongParameterList")
@Composable
internal fun CauriApp(
    uiState: MainUiState,
    onOnboardingCompleted: () -> Unit,
    notificationTarget: NotificationTarget?,
    onNotificationTargetShown: () -> Unit,
    lock: AppLockUiState,
    security: DeviceSecurity,
    onUnlocked: () -> Unit,
) {
    // Saved so that a configuration change (rotation, dark mode) does not show the launch screen again.
    var launchShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(LaunchDuration)
        launchShown = true
    }
    // The preferences load in a few milliseconds: the launch screen covers it.
    if (launchShown && uiState is MainUiState.Ready && lock.enabled != null) {
        // One help bubble at a time, whatever the screen.
        CompositionLocalProvider(LocalHelpBubbles provides hiltViewModel<HelpBubblesViewModel>()) {
            CauriNavHost(
                onboardingCompleted = uiState.onboardingCompleted,
                onOnboardingCompleted = onOnboardingCompleted,
                notificationTarget = notificationTarget,
                onNotificationTargetShown = onNotificationTargetShown,
            )
        }
        // Over the screens, which keep their state: it takes every touch until the user is recognised.
        if (lock.locked) {
            Box(Modifier.fillMaxSize().clickable(interactionSource = null, indication = null) {}) {
                LockScreen(security = security, onUnlocked = onUnlocked)
            }
        }
    } else {
        LaunchScreen()
    }
}

// Above the floating bottom bar.
private val MergeBarBottom = 100.dp

// The screen a notification asked for.
private fun NavHostController.open(target: NotificationTarget, onEdit: (Long) -> Unit, onQuickEntry: () -> Unit) =
    when (target) {
        NotificationTarget.CashCount -> navigateToCashCount()
        NotificationTarget.Settings -> navigateToSettings()
        NotificationTarget.SuggestedPlace -> navigateToSuggestedPlace()
        NotificationTarget.EveningSummaryScreen -> navigateToEveningSummary()
        // The sheets show over the screen the app opens on.
        is NotificationTarget.EditTransaction -> onEdit(target.transactionId)
        NotificationTarget.QuickEntry -> onQuickEntry()
    }

// The tabs of the bottom bar, in order, by the route of their graph.
private val TabRoutes = listOf(HomeGraph, ActivityGraph, GoalsGraph, InsightsGraph)

@Composable
private fun CauriNavHost(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    notificationTarget: NotificationTarget?,
    onNotificationTargetShown: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    // Chosen once: a new start destination would rebuild the graph under the back stack when the onboarding completes.
    val startDestination = remember { if (onboardingCompleted) HomeGraph else OnboardingGraph }
    var quickEntryOpen by rememberSaveable { mutableStateOf(false) }
    var editedTransaction by rememberSaveable { mutableStateOf<Long?>(null) }
    // A notification opens its screen, once the onboarding is done.
    LaunchedEffect(notificationTarget) {
        if (notificationTarget != null) {
            if (onboardingCompleted) {
                navController.open(
                    target = notificationTarget,
                    onEdit = { editedTransaction = it },
                    onQuickEntry = { quickEntryOpen = true },
                )
            }
            onNotificationTargetShown()
        }
    }
    val motion = CauriTheme.motion
    GlassScreen(
        // Drawn once behind every screen, which are transparent: glass cards and the bar blur these shapes.
        background = { Box(Modifier.fillMaxSize().backdrop(screenBackdropShapes())) },
        overlay = { TabsOverlay(navController = navController, onAdd = { quickEntryOpen = true }) },
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { if (betweenTabs()) motion.tabEnter() else motion.screenEnter() },
            exitTransition = { if (betweenTabs()) motion.tabExit() else motion.screenExit() },
            popEnterTransition = { if (betweenTabs()) motion.tabEnter() else motion.screenPopEnter() },
            popExitTransition = { if (betweenTabs()) motion.tabExit() else motion.screenPopExit() },
        ) {
            cauriGraphs(
                navController = navController,
                onOnboardingCompleted = onOnboardingCompleted,
                onEditTransaction = { editedTransaction = it },
            )
        }
    }
    if (quickEntryOpen) {
        QuickEntrySheet(onDismiss = { quickEntryOpen = false })
    }
    editedTransaction?.let { EditTransactionSheet(transactionId = it, onDismiss = { editedTransaction = null }) }
}

// The bottom bar, only on the tabs: it goes down during the onboarding and on screens opened from a tab; and the bar
// of a merge, above it.
@Composable
private fun BoxScope.TabsOverlay(navController: NavHostController, onAdd: () -> Unit) {
    val motion = CauriTheme.motion
    val destination = navController.currentBackStackEntryAsState().value?.destination
    val selectedTab = TabRoutes.indexOfFirst { route ->
        destination?.hierarchy?.any { it.hasRoute(route::class) } == true
    }
    AnimatedPresence(
        value = selectedTab.takeIf { it >= 0 },
        enter = motion.barEnter(),
        exit = motion.barExit(),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) { tab ->
        TabBar(
            selectedTab = tab,
            onTabSelected = { index -> navController.navigateToTab(TabRoutes[index]) },
            onAdd = onAdd,
        )
    }
    QuickEntryMergeBar(modifier = Modifier.align(Alignment.BottomCenter).aboveBottomBar())
}

// Every feature's graph; the features reach each other through these callbacks only.
private fun NavGraphBuilder.cauriGraphs(
    navController: NavHostController,
    onOnboardingCompleted: () -> Unit,
    onEditTransaction: (Long) -> Unit,
) {
    onboardingGraph(
        navController = navController,
        onCompleted = {
            onOnboardingCompleted()
            navController.navigate(HomeGraph) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        },
        onRestore = { navController.navigateToRestore() },
    )
    homeGraph(
        navController = navController,
        onCountCash = { navController.navigateToCashCount() },
        onOpenSettings = { navController.navigateToSettings() },
        onEditTransaction = onEditTransaction,
        onSplitBalanceGap = navController::navigateToBalanceGap,
    )
    activityGraph(onEditTransaction = onEditTransaction)
    goalsGraph()
    insightsGraph()
    cashGraph(navController = navController, onBack = { navController.popBackStack() })
    settingsGraph(
        navController = navController,
        onBack = { navController.popBackStack() },
        onOpenPlaces = { navController.navigateToPlaces() },
    )
    placesGraph(navController = navController, onBack = { navController.popBackStack() })
}

// Whether the navigation goes from a tab to another: tabs fade, side by side, instead of sliding.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs() =
    initialState.destination.isTab() && targetState.destination.isTab()

private fun NavDestination.isTab() = TabRoutes.any { route -> hierarchy.any { it.hasRoute(route::class) } }

// Where a bar floats over the screens: above the bottom bar, inside the screen's margins.
private fun Modifier.aboveBottomBar() =
    safeDrawingPadding().padding(start = CauriSpacing.xl, end = CauriSpacing.xl, bottom = MergeBarBottom)

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
