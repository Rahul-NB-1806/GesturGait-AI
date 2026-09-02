package com.example.gesturgaitai.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gesturgaitai.core.OfflineStorage
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gesturgaitai.components.AppleBackground
import com.example.gesturgaitai.screens.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Overview", Icons.Filled.Dashboard)
    data object Monitor : Screen("monitor", "Monitor", Icons.Filled.Sensors)
    data object History : Screen("history", "Trends", Icons.Filled.History)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    data object Tutorial : Screen("tutorial", "Guide", Icons.Filled.Dashboard)
}

val screens = listOf(Screen.Dashboard, Screen.Monitor, Screen.History, Screen.Settings)

@Composable
fun AppNavigation() {
    var isUserLoggedIn by remember { mutableStateOf(OfflineStorage.isLoggedIn()) }

    if (!isUserLoggedIn) {
        LoginScreen(onLoginSuccess = { _, _ ->
            isUserLoggedIn = true
        })
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AppleBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth(0.95f),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(32.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, 
                            Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.7f))
                                    )
                                )
                                .fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                screens.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    val activeColor = Color(0xFF007AFF)
                                    val inactiveColor = Color.Black.copy(alpha = 0.4f)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (selected) activeColor else inactiveColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Text(
                                            screen.label,
                                            fontSize = 10.sp,
                                            color = if (selected) activeColor else inactiveColor,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen() }
                composable(Screen.Monitor.route) { 
                    MonitoringScreen(onShowTutorial = { navController.navigate(Screen.Tutorial.route) }) 
                }
                composable(Screen.History.route) { HistoryScreen() }
                composable(Screen.Tutorial.route) { 
                    AccessibilityTutorialScreen(onDone = { navController.popBackStack() }) 
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onLogout = { isUserLoggedIn = false }
                    )
                }
            }
        }
    }
}
