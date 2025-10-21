package com.gamsung2.util

import androidx.navigation.NavController

/**
 * 네비게이션 예외로 앱이 죽지 않도록 막아주는 안전 이동 함수.
 * - 존재하지 않는 라우트, 잘못된 파라미터 등으로 발생하는 IllegalArgumentException 등을 캐치.
 * - popUpTo로 전체 백스택을 날리지 않도록 기본 정책 포함.
 */
inline fun NavController.navigateSafe(
    route: String,
    crossinline onError: (String) -> Unit = {}
) {
    runCatching {
        navigate(route) {
            launchSingleTop = true
            restoreState = true
            // popUpTo 설정은 기본 미적용(백스택 전체 제거 방지)
        }
    }.onFailure { t ->
        onError("화면 이동 실패: ${t::class.simpleName ?: "Unknown"}")
    }
}
