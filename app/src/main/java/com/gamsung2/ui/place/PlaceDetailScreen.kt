@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.place

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamsung2.ui.components.NetworkImage

@Composable
fun PlaceDetailScreen(
    placeId: String,
    title: String,
    companion: String,
    lat: Double?,
    lon: Double?,
    // ✅ 뷰모델/라우트에서 주입 가능. 기본값 유지 → 기존 호출부 안전.
    headerImageUrl: String? = null,
    onBack: () -> Unit,
    onNavigateToMap: (lat: Double?, lon: Double?, label: String?) -> Unit,
    onFindNearby: (category: String, lat: Double, lon: Double, aroundTitle: String) -> Unit = { _, _, _, _ -> }
) {
    // 스크롤 위치 복원 (placeId 기준)
    var savedScroll by rememberSaveable(placeId) { mutableStateOf(0) }
    val scrollState = rememberScrollState(initial = savedScroll)
    LaunchedEffect(scrollState.value) { savedScroll = scrollState.value }

    val hasCoord = (lat != null && lon != null)
    val safeTitle = title.ifBlank { "장소 상세" }
    val coordText = if (hasCoord) "좌표: ${"%.5f".format(lat)} , ${"%.5f".format(lon)}" else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(safeTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 히어로 이미지 (4:3 고정 비율 → 레이아웃 점프 방지)
            NetworkImage(
                url = headerImageUrl,
                ratio = 4f / 3f,
                cornerRadius = 16f,
                modifier = Modifier.fillMaxWidth()
            )

            // 핵심 정보
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = safeTitle, style = MaterialTheme.typography.titleLarge)
                    if (companion.isNotBlank()) {
                        Text(text = "방문 유형: $companion", style = MaterialTheme.typography.labelLarge)
                    }
                    Text(
                        text = "장소 ID: $placeId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    coordText?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                }
            }

            // 근처 찾기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { if (hasCoord) onFindNearby("lodging", lat!!, lon!!, safeTitle) },
                    modifier = Modifier.weight(1f),
                    enabled = hasCoord
                ) { Text("근처 숙소 찾기") }

                OutlinedButton(
                    onClick = { if (hasCoord) onFindNearby("restaurant", lat!!, lon!!, safeTitle) },
                    modifier = Modifier.weight(1f),
                    enabled = hasCoord
                ) { Text("근처 식당 찾기") }
            }

            // 소개
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("소개", style = MaterialTheme.typography.titleMedium)
                Text(
                    "이곳은 추천 명소입니다. 실제 서비스에서는 API의 상세 설명/편의시설/리뷰 요약 등을 표시합니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 지도
            Button(
                onClick = { onNavigateToMap(lat, lon, safeTitle) },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasCoord
            ) { Text("지도/길찾기") }
        }
    }
}
