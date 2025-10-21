package com.gamsung2.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * 앱 전역을 균일 비율로 스케일하는 래퍼.
 * dp/sp 모두 scale 배로 줄어듭니다. (예: 0.95f = 5% 축소)
 */
@Composable
fun ScaledApp(
    scale: Float = 0.95f,
    content: @Composable () -> Unit
) {
    val base = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = base.density * scale,    // dp 스케일
            fontScale = base.fontScale * scale // sp 스케일
        )
    ) {
        content()
    }
}
