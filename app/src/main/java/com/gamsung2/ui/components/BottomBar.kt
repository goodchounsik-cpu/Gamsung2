@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gamsung2.nav.Routes

/**
 * 하단 내비게이션 바
 * - 탭 이동은 singleTop + restoreState + popUpTo(start) 표준 패턴 사용
 * - 홈은 항상 HOME_NEAR로 고정 이동
 */
@Composable
fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest: NavDestination? = backStackEntry?.destination

    NavigationBar {

        // 홈
        NavigationBarItem(
            selected = currentDest.isRouteIn(Routes.HOME, Routes.HOME_NEAR, Routes.HOME_ALL),
            onClick = {
                if (currentDest.isRouteIn(Routes.HOME, Routes.HOME_NEAR, Routes.HOME_ALL)) return@NavigationBarItem
                navController.navigate(Routes.HOME_NEAR) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "홈") },
            label = { Text("홈") }
        )

        // 지도
        NavigationBarItem(
            selected = currentDest.isRouteIn(Routes.MAP),
            onClick = {
                if (currentDest.isRouteIn(Routes.MAP)) return@NavigationBarItem
                navController.navigate(Routes.MAP) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Map, contentDescription = "지도") },
            label = { Text("지도") }
        )

        // 즐겨찾기
        NavigationBarItem(
            selected = currentDest.isRouteIn(Routes.FAVORITES),
            onClick = {
                if (currentDest.isRouteIn(Routes.FAVORITES)) return@NavigationBarItem
                navController.navigate(Routes.FAVORITES) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "즐겨찾기") },
            label = { Text("즐겨찾기") }
        )

        // 번역
        NavigationBarItem(
            selected = currentDest.isRouteIn(Routes.TRANSLATE),
            onClick = {
                if (currentDest.isRouteIn(Routes.TRANSLATE)) return@NavigationBarItem
                navController.navigate(Routes.TRANSLATE) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Translate, contentDescription = "번역") },
            label = { Text("번역") }
        )
    }
}

/** 현재 목적지가 주어진 route 중 하나와 일치하는지 (쿼리스트링 무시) */
private fun NavDestination?.isRouteIn(vararg routes: String): Boolean {
    if (this == null) return false
    val set = routes.toSet()
    return hierarchy.any { dest ->
        val r = dest.route ?: return@any false
        r in set || r.substringBefore('?') in set
    }
}
