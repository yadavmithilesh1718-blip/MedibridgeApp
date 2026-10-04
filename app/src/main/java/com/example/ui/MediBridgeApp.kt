package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.alerts.AlertsScreen
import com.example.ui.screens.chat.GeminiChatScreen
import com.example.ui.screens.donate.DonateScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.impact.ImpactScreen
import com.example.ui.screens.manage.ManageDonationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.recipients.RecipientsScreen
import com.example.ui.screens.web.WebPortalScreen
import com.example.ui.viewmodel.MediBridgeViewModel

enum class MediBridgeNavScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    DONATE("Donate", Icons.Filled.AddCircle, Icons.Outlined.AddCircle, "nav_donate"),
    RECIPIENTS("Partners", Icons.Filled.Verified, Icons.Outlined.Verified, "nav_recipients"),
    TRACKER("Tracker", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_tracker"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile"),
    ASSISTANT("MediBot", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "nav_assistant"),
    ALERTS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_alerts"),
    IMPACT("Impact", Icons.Filled.Eco, Icons.Outlined.Eco, "nav_impact"),
    WEB_PORTAL("Web View", Icons.Filled.Language, Icons.Outlined.Language, "nav_web_portal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediBridgeApp(
    viewModel: MediBridgeViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(MediBridgeNavScreen.HOME) }
    val unreadAlertsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    // Handle system back navigation to return to Home screen
    BackHandler(enabled = currentScreen != MediBridgeNavScreen.HOME) {
        currentScreen = MediBridgeNavScreen.HOME
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // TABLET / DESKTOP / CHROMEBOOK: Navigation Rail Layout
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .testTag("tablet_navigation_rail"),
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MediBridge",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    MediBridgeNavScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                if (screen == MediBridgeNavScreen.ALERTS && unreadAlertsCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$unreadAlertsCount") } }) {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            modifier = Modifier.testTag("${screen.testTag}_rail")
                        )
                    }
                }

                // Wide Content Area with TopBar and Centered Content
                Scaffold(
                    modifier = Modifier.weight(1f),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentScreen.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                if (currentScreen != MediBridgeNavScreen.PROFILE) {
                                    IconButton(
                                        onClick = { currentScreen = MediBridgeNavScreen.PROFILE },
                                        modifier = Modifier.testTag("topbar_profile_button_wide")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AccountCircle,
                                            contentDescription = "Profile & Donation History",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (currentScreen != MediBridgeNavScreen.ASSISTANT) {
                                    IconButton(
                                        onClick = { currentScreen = MediBridgeNavScreen.ASSISTANT },
                                        modifier = Modifier.testTag("topbar_chat_button_wide")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.SmartToy,
                                            contentDescription = "Gemini Assistant",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (currentScreen != MediBridgeNavScreen.WEB_PORTAL) {
                                    IconButton(
                                        onClick = { currentScreen = MediBridgeNavScreen.WEB_PORTAL },
                                        modifier = Modifier.testTag("topbar_web_portal_button_wide")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Language,
                                            contentDescription = "Web Application",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { currentScreen = MediBridgeNavScreen.ALERTS },
                                    modifier = Modifier.testTag("topbar_alerts_button_wide")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadAlertsCount > 0) {
                                                Badge { Text("$unreadAlertsCount") }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (currentScreen == MediBridgeNavScreen.ALERTS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                            contentDescription = "Alerts"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 1100.dp)
                        ) {
                            ScreenContent(
                                currentScreen = currentScreen,
                                viewModel = viewModel,
                                onNavigateTo = { currentScreen = it }
                            )
                        }
                    }
                }
            }
        } else {
            // MOBILE HANDHELD: Bottom Navigation Layout
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MediBridge",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            // Profile & Donation History Shortcut
                            IconButton(
                                onClick = { currentScreen = MediBridgeNavScreen.PROFILE },
                                modifier = Modifier.testTag("topbar_profile_button")
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == MediBridgeNavScreen.PROFILE) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                    contentDescription = "Profile & Donation History",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Gemini AI Assistant
                            IconButton(
                                onClick = { currentScreen = MediBridgeNavScreen.ASSISTANT },
                                modifier = Modifier.testTag("topbar_chat_button")
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == MediBridgeNavScreen.ASSISTANT) Icons.Filled.SmartToy else Icons.Outlined.SmartToy,
                                    contentDescription = "Gemini Assistant",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Web Application Portal
                            IconButton(
                                onClick = { currentScreen = MediBridgeNavScreen.WEB_PORTAL },
                                modifier = Modifier.testTag("topbar_web_portal_button")
                            ) {
                                Icon(
                                    imageVector = if (currentScreen == MediBridgeNavScreen.WEB_PORTAL) Icons.Filled.Language else Icons.Outlined.Language,
                                    contentDescription = "Web Application",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Alerts
                            IconButton(
                                onClick = { currentScreen = MediBridgeNavScreen.ALERTS },
                                modifier = Modifier.testTag("topbar_alerts_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadAlertsCount > 0) {
                                            Badge {
                                                Text("$unreadAlertsCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentScreen == MediBridgeNavScreen.ALERTS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                        contentDescription = "Alerts"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    // Mobile navigation items (Primary 5 tabs)
                    val mobileTabs = listOf(
                        MediBridgeNavScreen.HOME,
                        MediBridgeNavScreen.DONATE,
                        MediBridgeNavScreen.TRACKER,
                        MediBridgeNavScreen.WEB_PORTAL,
                        MediBridgeNavScreen.PROFILE
                    )

                    NavigationBar(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("bottom_navigation_bar"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        mobileTabs.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                modifier = Modifier.testTag(screen.testTag)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ScreenContent(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        onNavigateTo = { currentScreen = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentScreen: MediBridgeNavScreen,
    viewModel: MediBridgeViewModel,
    onNavigateTo: (MediBridgeNavScreen) -> Unit
) {
    when (currentScreen) {
        MediBridgeNavScreen.HOME -> HomeScreen(
            viewModel = viewModel,
            onNavigateToDonate = { onNavigateTo(MediBridgeNavScreen.DONATE) },
            onNavigateToRecipients = { onNavigateTo(MediBridgeNavScreen.RECIPIENTS) },
            onNavigateToManage = { onNavigateTo(MediBridgeNavScreen.TRACKER) },
            onNavigateToImpact = { onNavigateTo(MediBridgeNavScreen.IMPACT) },
            onNavigateToAlerts = { onNavigateTo(MediBridgeNavScreen.ALERTS) },
            onNavigateToWebPortal = { onNavigateTo(MediBridgeNavScreen.WEB_PORTAL) }
        )
        MediBridgeNavScreen.DONATE -> DonateScreen(
            viewModel = viewModel,
            onDonationSuccess = { onNavigateTo(MediBridgeNavScreen.TRACKER) }
        )
        MediBridgeNavScreen.RECIPIENTS -> RecipientsScreen(
            viewModel = viewModel,
            onDonateForRequest = { requestedMedicine ->
                viewModel.updateMedicineName(requestedMedicine)
                viewModel.runAiOcrAnalysis()
                onNavigateTo(MediBridgeNavScreen.DONATE)
            }
        )
        MediBridgeNavScreen.TRACKER -> ManageDonationsScreen(
            viewModel = viewModel,
            onNavigateToDonate = { onNavigateTo(MediBridgeNavScreen.DONATE) }
        )
        MediBridgeNavScreen.PROFILE -> ProfileScreen(
            viewModel = viewModel,
            onNavigateToDonate = { onNavigateTo(MediBridgeNavScreen.DONATE) },
            onNavigateToTracker = { onNavigateTo(MediBridgeNavScreen.TRACKER) }
        )
        MediBridgeNavScreen.ASSISTANT -> GeminiChatScreen(
            viewModel = viewModel
        )
        MediBridgeNavScreen.ALERTS -> AlertsScreen(
            viewModel = viewModel
        )
        MediBridgeNavScreen.IMPACT -> ImpactScreen(
            viewModel = viewModel
        )
        MediBridgeNavScreen.WEB_PORTAL -> WebPortalScreen(
            viewModel = viewModel
        )
    }
}
