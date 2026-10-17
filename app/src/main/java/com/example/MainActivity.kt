package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainScreen
import com.example.ui.StockViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: StockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockismTheme {
                StockismApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockismApp(viewModel: StockViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val alerts by viewModel.repository.alerts.collectAsState()
    val unreadAlertsCount = remember(alerts) { alerts.count { !it.isRead } }

    // Intercept back button on secondary screens
    BackHandler(enabled = currentScreen != MainScreen.Home) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Wordmark & live dot
                        Text(
                            text = "STOCK-ISM",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .background(GainGreenBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(GainGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GainGreen
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(MainScreen.Scanner) },
                        modifier = Modifier.testTag("top_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search & Scan",
                            tint = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(MainScreen.Settings) },
                        modifier = Modifier.testTag("top_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentScreen == MainScreen.Settings) BrandTeal else TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Home
                NavigationBarItem(
                    selected = currentScreen == MainScreen.Home,
                    onClick = { viewModel.navigateTo(MainScreen.Home) },
                    icon = {
                        Icon(
                            if (currentScreen == MainScreen.Home) Icons.Filled.ShowChart else Icons.Outlined.ShowChart,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryNeon,
                        indicatorColor = PrimaryNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // Signals
                NavigationBarItem(
                    selected = currentScreen == MainScreen.Signals,
                    onClick = { viewModel.navigateTo(MainScreen.Signals) },
                    icon = {
                        Icon(
                            if (currentScreen == MainScreen.Signals) Icons.Filled.Insights else Icons.Outlined.Insights,
                            contentDescription = "Signals"
                        )
                    },
                    label = { Text("Signals", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryNeon,
                        indicatorColor = PrimaryNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_signals")
                )

                // Scanner
                NavigationBarItem(
                    selected = currentScreen == MainScreen.Scanner,
                    onClick = { viewModel.navigateTo(MainScreen.Scanner) },
                    icon = {
                        Icon(
                            if (currentScreen == MainScreen.Scanner) Icons.Filled.FilterList else Icons.Outlined.FilterList,
                            contentDescription = "Scanner"
                        )
                    },
                    label = { Text("Scanner", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryNeon,
                        indicatorColor = PrimaryNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_scanner")
                )

                // Portfolio
                NavigationBarItem(
                    selected = currentScreen == MainScreen.Portfolio,
                    onClick = { viewModel.navigateTo(MainScreen.Portfolio) },
                    icon = {
                        Icon(
                            if (currentScreen == MainScreen.Portfolio) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Portfolio"
                        )
                    },
                    label = { Text("Portfolio", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryNeon,
                        indicatorColor = PrimaryNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_portfolio")
                )

                // Alerts
                NavigationBarItem(
                    selected = currentScreen == MainScreen.Alerts,
                    onClick = { viewModel.navigateTo(MainScreen.Alerts) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadAlertsCount > 0) {
                                    Badge(containerColor = HeatAmber) {
                                        Text("$unreadAlertsCount", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (currentScreen == MainScreen.Alerts) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Alerts"
                            )
                        }
                    },
                    label = { Text("Alerts", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryNeon,
                        indicatorColor = PrimaryNeon,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_alerts")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MainScreen.Home -> HomeScreen(viewModel = viewModel)
                MainScreen.Signals -> SignalsScreen(viewModel = viewModel)
                MainScreen.Scanner -> ScannerScreen(viewModel = viewModel)
                MainScreen.Detail -> DetailScreen(viewModel = viewModel)
                MainScreen.Portfolio -> PortfolioScreen(viewModel = viewModel)
                MainScreen.Alerts -> AlertsScreen(viewModel = viewModel)
                MainScreen.Settings -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
