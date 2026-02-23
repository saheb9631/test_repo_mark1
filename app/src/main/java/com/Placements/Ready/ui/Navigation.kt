package com.Placements.Ready.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.Placements.Ready.ui.screens.*
import com.Placements.Ready.ui.theme.*
import com.Placements.Ready.viewmodel.AuthViewModel
import com.Placements.Ready.viewmodel.PlacementViewModel
import com.Placements.Ready.viewmodel.QuestionViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Placements : Screen("placements", "Placements", Icons.Outlined.Business, Icons.Filled.Business)
    object Planner : Screen("planner", "Planner", Icons.Outlined.Code, Icons.Filled.Code)
    object Resume : Screen("resume", "Resume", Icons.Outlined.Description, Icons.Filled.Description)
    object More : Screen("more", "More", Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz)
}

sealed class SubScreen(val route: String) {
    object CompanyDetail : SubScreen("company_detail/{companyName}")
    object SemesterMonths : SubScreen("semester_months/{semesterId}")
    object PlacementMonth : SubScreen("placement_month/{year}/{month}")
    object Roadmap : SubScreen("roadmap")
    object Portfolio : SubScreen("portfolio")
    object Settings : SubScreen("settings")
    object Onboarding : SubScreen("onboarding")
    object QuestionDetail : SubScreen("question_detail/{questionId}")
    object ProgressDashboard : SubScreen("progress_dashboard")
    object DailyPlan : SubScreen("daily_plan")
    object RevisionList : SubScreen("revision_list")
    object FavoritesList : SubScreen("favorites_list")
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Placements,
    Screen.Planner,
    Screen.Resume,
    Screen.More
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusPlacementApp() {
    val navController = rememberNavController()
    val viewModel: PlacementViewModel = viewModel()
    val questionViewModel: QuestionViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val authManager = remember { com.Placements.Ready.data.AuthManager() }

    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn = currentUser != null

    // We only need verification if we're logged in AND email is not verified
    var isCheckingVerification by remember(currentUser) {
        mutableStateOf(currentUser?.isEmailVerified == false)
    }

    var pendingEmail by remember(currentUser) {
        mutableStateOf(currentUser?.email ?: "")
    }
    var pendingPassword by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Determine which screen to show
    val screenKey = when {
        !isLoggedIn -> "login"
        isCheckingVerification -> "verification"
        else -> "main"
    }

    // Use Crossfade for smooth transitions between auth states
    androidx.compose.animation.Crossfade(
        targetState = screenKey,
        animationSpec = tween(300),
        label = "authTransition"
    ) { state ->
        when (state) {
            "login" -> {
                LoginScreen(
                    onLoginSuccess = { verified, email, pw ->
                        pendingPassword = pw
                    }
                )
            }
            "verification" -> {
                VerificationScreen(
                    email = pendingEmail,
                    password = pendingPassword,
                    viewModel = authViewModel,
                    onVerified = { isCheckingVerification = false },
                    onBack = {
                        authViewModel.signOut(context)
                        pendingPassword = ""
                    }
                )
            }
            "main" -> {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

                if (showBottomBar) {
                    PremiumBottomBar(
                        items = bottomNavItems,
                        currentRoute = currentDestination?.route,
                        onItemClick = { screen ->
                            if (currentDestination?.route != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(250)) +
                        slideInVertically(initialOffsetY = { 30 }, animationSpec = tween(250))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(200))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(250))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(200)) +
                        slideOutVertically(targetOffsetY = { 30 }, animationSpec = tween(200))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        questionViewModel = questionViewModel,
                        navController = navController
                    )
                }
                composable(Screen.Placements.route) {
                    SemesterScreen(navController = navController)
                }
                composable(Screen.Planner.route) {
                    QuestionListScreen(
                        viewModel = questionViewModel,
                        navController = navController
                    )
                }
                composable(Screen.Resume.route) {
                    ResumeScreen()
                }
                composable(Screen.More.route) {
                    MoreScreen(navController = navController)
                }
                composable(SubScreen.SemesterMonths.route) { backStackEntry ->
                    val semesterId = backStackEntry.arguments?.getString("semesterId") ?: ""
                    SemesterMonthsScreen(
                        semesterId = semesterId,
                        viewModel = viewModel,
                        navController = navController,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(SubScreen.PlacementMonth.route) { backStackEntry ->
                    val year = backStackEntry.arguments?.getString("year") ?: ""
                    val month = backStackEntry.arguments?.getString("month") ?: ""
                    PlacementScreen(
                        viewModel = viewModel,
                        navController = navController,
                        year = year,
                        month = month
                    )
                }
                composable(SubScreen.CompanyDetail.route) { backStackEntry ->
                    val companyName = backStackEntry.arguments?.getString("companyName") ?: ""
                    CompanyDetailScreen(
                        companyName = companyName,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(SubScreen.Roadmap.route) {
                    RoadmapScreen(onBack = { navController.popBackStack() })
                }
                composable(SubScreen.Portfolio.route) {
                    PortfolioScreen(onBack = { navController.popBackStack() })
                }
                composable(SubScreen.Settings.route) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onSignOut = {
                            authViewModel.signOut(context)
                        }
                    )
                }
                composable(SubScreen.QuestionDetail.route) { backStackEntry ->
                    val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
                    QuestionDetailScreen(
                        questionId = questionId,
                        viewModel = questionViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(SubScreen.ProgressDashboard.route) {
                    ProgressDashboardScreen(
                        viewModel = questionViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(SubScreen.DailyPlan.route) {
                    DailyPlanScreen(
                        viewModel = questionViewModel,
                        navController = navController,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
            } // end "main"
        } // end when
    } // end Crossfade
}

@Composable
fun PremiumBottomBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                clip = false,
                ambientColor = NeonPurple.copy(alpha = 0.1f),
                spotColor = NeonPurple.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                PremiumNavItem(
                    screen = screen,
                    selected = selected,
                    onClick = { onItemClick(screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PremiumNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    val pillAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(250),
        label = "pillAlpha"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) NeonPurple else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "textColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 40.dp, color = NeonPurple.copy(alpha = 0.3f))
            )
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    NeonPurple.copy(alpha = 0.18f * pillAlpha)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) screen.selectedIcon else screen.icon,
                contentDescription = screen.title,
                tint = textColor,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = screen.title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            maxLines = 1
        )
    }
}
