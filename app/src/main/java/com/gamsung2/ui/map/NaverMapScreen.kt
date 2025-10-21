@file:Suppress("unused", "SpellCheckingInspection") // Stub이라 미사용/맞춤법 경고 전체 억제
@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamsung2.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.MapViewModel

/**
 * 네이버 지도 자리표시자(Stub).
 * - SDK/키 준비 전: 이 화면을 사용 (컴파일 OK)
 * - 준비 완료 후: 실제 NaverMap 구현 파일로 교체
 */
@Composable
fun NaverMapScreen(
    vm: MapViewModel,
    onBack: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    focusLat: Double? = null,
    focusLng: Double? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("네이버 지도 (준비중)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
        ) {
            Text(
                "네이버 지도 기능은 현재 비활성화되어 있습니다.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "준비되면:\n" +
                        "1) build.gradle에 com.naver.maps:map-sdk 추가\n" +
                        "2) Manifest에 CLIENT_ID(meta-data) + strings.xml 설정\n" +
                        "3) BuildConfig.NAVER_MAP_ENABLED = true\n" +
                        "4) Stub 파일을 실제 구현으로 교체"
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack) { Text("돌아가기") }
        }
    }
}
