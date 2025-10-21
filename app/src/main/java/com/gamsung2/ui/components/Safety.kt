package com.gamsung2.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController

private const val TAG = "SafeAction"

suspend fun SnackbarHostState.showShort(message: String) {
    showSnackbar(message)
}

/** NavController 안전 네비게이션 */
suspend fun NavHostController.safeNavigate(
    route: String?,
    snackbar: SnackbarHostState? = null,
    onFailMsg: String = "아직 준비되지 않았습니다."
) {
    try {
        require(!route.isNullOrBlank()) { "Empty route" }
        navigate(route)
    } catch (t: Throwable) {
        Log.e(TAG, "navigate fail: $route", t)
        snackbar?.showShort(onFailMsg)
    }
}

/** 인텐트 안전 실행 */
suspend fun Context.safeStartActivity(
    intent: Intent,
    snackbar: SnackbarHostState? = null,
    onFailMsg: String = "열 수 있는 앱이 없습니다."
) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "startActivity fail", e)
        snackbar?.showShort(onFailMsg)
    } catch (t: Throwable) {
        Log.e(TAG, "startActivity fail", t)
        snackbar?.showShort("실행 중 오류가 발생했습니다.")
    }
}
