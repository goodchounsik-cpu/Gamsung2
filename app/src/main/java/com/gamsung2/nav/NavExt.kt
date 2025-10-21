package com.gamsung2.nav

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavDestination.Companion.hierarchy

/** 쿼리 파라미터 전 제거한 '베이스 경로' */
private fun String.baseRoute(): String = substringBefore('?')

/**
 * 같은 화면(베이스 경로 기준)으로의 중복 네비게이션을 차단하는 안전 네비게이션.
 * 예: "map" 과 "map?lat=..." 은 같은 화면으로 간주.
 */
fun NavController.safeNavigate(route: String) {
    val currentBase = currentDestination?.route?.baseRoute()
    val targetBase  = route.baseRoute()
    if (currentBase == targetBase) return  // 중복 이동 방지
    try {
        navigate(route)
    } catch (_: IllegalArgumentException) {
        // 빠른 연타 등으로 발생하는 중복 네비게이션 예외는 조용히 무시
    }
}

/** NavOptionsBuilder 버전 (popUpTo/restoreState 등 옵션을 줄 때 사용) */
fun NavController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit
) {
    val currentBase = currentDestination?.route?.baseRoute()
    val targetBase  = route.baseRoute()
    if (currentBase == targetBase) return
    try {
        navigate(route, builder)
    } catch (_: IllegalArgumentException) {
        // 동일
    }
}

/** 현재 목적지가 주어진 route 들(쿼리 포함/제외)에 해당하는지 */
fun NavDestination?.isRouteIn(vararg routes: String): Boolean {
    if (this == null) return false
    val bases = routes.map { it.baseRoute() }.toSet()
    return hierarchy.any { dest ->
        val r = dest.route ?: return@any false
        r in routes || r.baseRoute() in bases
    }
}
