package nisargpatel.deadreckoning.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import nisargpatel.deadreckoning.data.MockNavigationRepository
import nisargpatel.deadreckoning.domain.state.NavigationSession
import nisargpatel.deadreckoning.ui.screens.*
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Onboarding : Screen("onboarding", "Onboarding")
    object Permission : Screen("permission", "Permission")
    object Calibration : Screen("calibration", "Calibration")

    // 6 Primary Bottom Nav Destinations
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Navigation : Screen("navigation", "Nav", Icons.Default.Navigation)
    object Intelligence : Screen("intelligence", "AI", Icons.Default.Psychology)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Analytics)
    object Sessions : Screen("sessions", "Sessions", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Technical Secondary Screens
    object Sensors : Screen("sensors", "Sensors")
    object GNSS : Screen("gnss", "GNSS")
    object DeadReckoning : Screen("dead_reckoning", "Dead Reckoning")
    object MapMatching : Screen("map_matching", "Map Matching")
    object Trajectory : Screen("trajectory", "Trajectory")
    object OfflineMaps : Screen("offline_maps", "Offline Maps")
    object SessionDetail : Screen("session_detail", "Session Detail")
    object Diagnostics : Screen("diagnostics", "Diagnostics")
}

@Composable
fun IDRAppShell() {
    val context = LocalContext.current
    val repository = remember { MockNavigationRepository(context) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Navigation,
        Screen.Intelligence,
        Screen.Analytics,
        Screen.Sessions,
        Screen.Settings
    )

    var selectedDetailSession by remember { mutableStateOf<NavigationSession?>(null) }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    IDRTheme {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = AutomotiveDarkBg,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                                label = { Text(screen.title, fontSize = 10.sp) },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlue.copy(alpha = 0.2f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(onSplashFinished = { navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } } })
                }
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(onOnboardingFinished = { navController.navigate(Screen.Permission.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } } })
                }
                composable(Screen.Permission.route) {
                    PermissionScreen(onContinue = { navController.navigate(Screen.Calibration.route) { popUpTo(Screen.Permission.route) { inclusive = true } } })
                }
                composable(Screen.Calibration.route) {
                    CalibrationScreen(onStartNavigation = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Calibration.route) { inclusive = true } } })
                }

                // 6 Main Screens with Screen-Specific ViewModels
                composable(Screen.Home.route) {
                    val viewModel = viewModel<HomeViewModel> { HomeViewModel(repository) }
                    HomeScreen(viewModel = viewModel, onStartNavClicked = { navController.navigate(Screen.Navigation.route) })
                }
                composable(Screen.Navigation.route) {
                    val viewModel = viewModel<NavigationViewModel> { NavigationViewModel(repository) }
                    LiveNavigationScreen(viewModel = viewModel)
                }
                composable(Screen.Intelligence.route) {
                    val viewModel = viewModel<IntelligenceViewModel> { IntelligenceViewModel(repository) }
                    IntelligenceScreen(viewModel = viewModel)
                }
                composable(Screen.Analytics.route) {
                    val viewModel = viewModel<AnalyticsViewModel> { AnalyticsViewModel(repository) }
                    AnalyticsScreen(viewModel = viewModel)
                }
                composable(Screen.Sessions.route) {
                    val viewModel = viewModel<SessionsViewModel> { SessionsViewModel(repository) }
                    SessionsScreen(viewModel = viewModel, onSessionSelected = { session ->
                        selectedDetailSession = session
                        navController.navigate(Screen.SessionDetail.route)
                    })
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onNavigateToTechnicalScreen = { route ->
                        navController.navigate(route)
                    })
                }

                // Technical Secondary Screens
                composable(Screen.Sensors.route) {
                    val viewModel = viewModel<SensorsViewModel> { SensorsViewModel(repository) }
                    SensorsScreen(viewModel = viewModel)
                }
                composable(Screen.GNSS.route) {
                    val viewModel = viewModel<GNSSViewModel> { GNSSViewModel(repository) }
                    GNSSScreen(viewModel = viewModel)
                }
                composable(Screen.DeadReckoning.route) {
                    val viewModel = viewModel<NavigationViewModel> { NavigationViewModel(repository) }
                    DeadReckoningScreen(viewModel = viewModel)
                }
                composable(Screen.MapMatching.route) {
                    val viewModel = viewModel<NavigationViewModel> { NavigationViewModel(repository) }
                    MapMatchingScreen(viewModel = viewModel)
                }
                composable(Screen.Trajectory.route) {
                    val viewModel = viewModel<NavigationViewModel> { NavigationViewModel(repository) }
                    TrajectoryScreen(viewModel = viewModel)
                }
                composable(Screen.OfflineMaps.route) {
                    OfflineMapsScreen()
                }
                composable(Screen.SessionDetail.route) {
                    selectedDetailSession?.let { session ->
                        SessionDetailScreen(session = session, onBack = { navController.popBackStack() })
                    } ?: LaunchedEffect(Unit) { navController.popBackStack() }
                }
                composable(Screen.Diagnostics.route) {
                    val viewModel = viewModel<DiagnosticsViewModel> { DiagnosticsViewModel(repository) }
                    DiagnosticsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
