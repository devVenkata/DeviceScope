package com.devicescope.app.presentation.navigation


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.devicescope.app.presentation.dashboard.DashboardScreen
import com.devicescope.app.presentation.device.DeviceScreen
import com.devicescope.app.presentation.battery.BatteryScreen

private data class NavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val navItems = listOf(
    NavItem("Home", Icons.Default.Home),
    NavItem("Usage", Icons.Default.ShowChart),
    NavItem("Diagnostic", Icons.Default.Build),
    NavItem("History", Icons.Default.History),
    NavItem("Settings", Icons.Default.Settings)
)

@Composable
fun AppNavigation() {
    var selectedTab by remember { mutableStateOf("Home") }
    var currentScreen by remember { mutableStateOf("dashboard") }

    BackHandler(enabled = currentScreen != "dashboard") {
        currentScreen = "dashboard"
        selectedTab = "Home"
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.title,
                        onClick = {
                            selectedTab = item.title

                            currentScreen = when (item.title) {
                                "Home" -> "dashboard"
                                "Usage" -> "usage"
                                "Diagnostic" -> "diagnostic"
                                "History" -> "history"
                                "Settings" -> "settings"
                                else -> "dashboard"
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen(
                    onDeviceClick = {
                        currentScreen = "device"
                    },
                    onBatteryClick = {
                        currentScreen = "battery"
                    }
                )

                "device" -> DeviceScreen()
                "battery" -> BatteryScreen()

                "usage" -> PlaceholderScreen("Usage")
                "diagnostic" -> PlaceholderScreen("Diagnostic")
                "history" -> PlaceholderScreen("History")
                "settings" -> PlaceholderScreen("Settings")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "$title screen coming soon",
            style = MaterialTheme.typography.titleMedium
        )
    }
}