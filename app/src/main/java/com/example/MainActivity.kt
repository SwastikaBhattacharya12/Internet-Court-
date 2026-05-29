package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.size
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.InfoCenterScreen
import com.example.ui.screens.JuryRoomScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SubmitCaseScreen
import androidx.compose.material.icons.filled.Mic
import com.example.ui.theme.*
import com.example.ui.viewmodel.CourtViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout()
            }
        }
    }
}

@Composable
fun MainLayout() {
    val navController = rememberNavController()
    val courtViewModel: CourtViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "feed"

    val navigationItems = listOf(
        NavigationItem("feed", "Docket", Icons.Default.Gavel),
        NavigationItem("live_jury", "Live", Icons.Default.Mic),
        NavigationItem("submit", "File", Icons.Default.AddCircle),
        NavigationItem("quiz", "Learn", Icons.Default.LocalFireDepartment),
        NavigationItem("leaderboard", "League", Icons.Default.EmojiEvents)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Highly modern Material 3 Navigation Bar matching edge-to-edge guidelines
            NavigationBar(
                containerColor = MidnightSlate,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                navigationItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MidnightSlate else TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) CourtroomGold else TextGray,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    letterSpacing = 0.3.sp
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Clip
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CourtroomGold,
                            unselectedTextColor = TextGray,
                            selectedTextColor = CourtroomGold
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "feed",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("feed") {
                FeedScreen(
                    viewModel = courtViewModel,
                    onTabSelected = { tabName ->
                        navController.navigate(tabName)
                    }
                )
            }
            composable("live_jury") {
                JuryRoomScreen(
                    viewModel = courtViewModel,
                    onTabSelected = { tabName ->
                        navController.navigate(tabName)
                    }
                )
            }
            composable("submit") {
                SubmitCaseScreen(
                    viewModel = courtViewModel,
                    onSuccessSubmitted = {
                        // Return to feed on completion!
                        navController.navigate("feed") {
                            popUpTo("feed") { inclusive = true }
                        }
                    }
                )
            }
            composable("quiz") {
                QuizScreen(viewModel = courtViewModel)
            }
            composable("leaderboard") {
                LeaderboardScreen(viewModel = courtViewModel)
            }
            composable("info") {
                InfoCenterScreen(viewModel = courtViewModel)
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
