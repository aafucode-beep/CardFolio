package com.cardfolio.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cardfolio.ui.screens.CardDetailScreen
import com.cardfolio.ui.screens.CardListScreen
import com.cardfolio.ui.screens.CameraScreen
import com.cardfolio.ui.screens.SearchScreen
import com.cardfolio.ui.screens.StatsScreen
import com.cardfolio.ui.screens.UploadResultScreen

object Routes {
    const val LIST = "list"
    const val CAMERA = "camera"
    const val SEARCH = "search"
    const val STATS = "stats"
    const val DETAIL = "detail/{cardId}"
    const val UPLOAD_RESULT = "upload_result/{cardId}"

    fun detail(cardId: String) = "detail/$cardId"
    fun uploadResult(cardId: String) = "upload_result/$cardId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()

    val bottomItems = listOf(
        BottomNavItem(Routes.LIST, "名片", { Icon(Icons.Default.GridView, contentDescription = null) }),
        BottomNavItem(Routes.CAMERA, "拍照", { Icon(Icons.Default.CameraAlt, contentDescription = null) }),
        BottomNavItem(Routes.SEARCH, "搜索", { Icon(Icons.Default.Search, contentDescription = null) }),
        BottomNavItem(Routes.STATS, "统计", { Icon(Icons.Default.Add, contentDescription = null) }),
    )

    val navStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Routes.LIST, Routes.CAMERA, Routes.SEARCH, Routes.STATS)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            icon = item.icon,
                            label = { /* text hidden for compactness */ },
                            selected = navStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LIST,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.LIST) {
                CardListScreen(
                    onCardClick = { cardId -> navController.navigate(Routes.detail(cardId)) },
                    onFabClick = { navController.navigate(Routes.CAMERA) },
                )
            }
            composable(Routes.CAMERA) {
                CameraScreen(
                    onUploaded = { cardId -> navController.navigate(Routes.uploadResult(cardId)) },
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onCardClick = { cardId -> navController.navigate(Routes.detail(cardId)) },
                )
            }
            composable(Routes.STATS) {
                StatsScreen()
            }
            composable(Routes.DETAIL) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                CardDetailScreen(
                    cardId = cardId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.UPLOAD_RESULT) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                UploadResultScreen(
                    cardId = cardId,
                    onDone = { navController.navigate(Routes.LIST) { popUpTo(0) } },
                )
            }
        }
    }
}
