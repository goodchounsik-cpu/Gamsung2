// app/src/main/java/com/gamsung2/nav/DeepLinkNavigator.kt
package com.gamsung2.nav

import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.navOptions

/**
 * app:// 스킴 딥링크를 앱 내부 라우트로 연결하는 유틸.
 *
 * 지원 예시:
 * - app://festival/{festivalId}                     → THEME_DETAIL (key=festivalId, title=축제)
 * - app://event/{eventId}                           → THEME_DETAIL (key=eventId,  title=행사)
 * - app://place/{placeId}?title=...&companion=...   → PLACE_DETAIL
 *
 * 필요 시 when 블록에 케이스 추가하여 확장하세요.
 */
fun handleAppDeepLink(navController: NavHostController, link: String) {
    val uri = runCatching { Uri.parse(link) }.getOrNull() ?: return
    if (uri.scheme != "app") return

    val segments = uri.pathSegments ?: emptyList()
    val first = segments.getOrNull(0) ?: return
    val id = segments.getOrNull(1).orEmpty()

    // 중복 네비게이션/백스택 누적 최소화 옵션
    val opts = navOptions {
        launchSingleTop = true
        restoreState = true
        // 필요 시 특정 목적지로 popUpTo 설정 가능
        // popUpTo(Routes.HOME) { inclusive = false }
    }

    when (first) {
        // 예: app://festival/1 → THEME_DETAIL (축제)
        "festival" -> {
            val title = "축제"
            navController.navigate(
                "${Routes.THEME_DETAIL}?key=${id.e()}&title=${title.e()}",
                opts
            )
        }

        // 예: app://event/41 → THEME_DETAIL (행사)
        "com/gamsung2/ui/event" -> {
            val title = "행사"
            navController.navigate(
                "${Routes.THEME_DETAIL}?key=${id.e()}&title=${title.e()}",
                opts
            )
        }

        // 예: app://place/abcd123?title=카페&companion=친구
        "place" -> {
            val title = uri.getQueryParameter("title").orEmpty()
            val companion = uri.getQueryParameter("companion").orEmpty()
            navController.navigate(
                Routes.placeDetailRoute(
                    placeId = id.e(),
                    companion = companion.e(),
                    title = title.e()
                ),
                opts
            )
        }

        // TODO: 필요한 스킴들 추가 (예: translate, map 등)
        else -> {
            // 정의되지 않은 경로는 무시하거나 공용 화면으로 유도할 수 있음.
            // navController.navigate(Routes.HOME, opts)
        }
    }
}

/** 라우트 파라미터용 안전 인코딩 */
private fun String.e(): String = Uri.encode(this)
