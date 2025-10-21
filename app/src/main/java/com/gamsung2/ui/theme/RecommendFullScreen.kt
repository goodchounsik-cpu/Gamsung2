@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gamsung2.nav.Routes

/* 지역 센터(ThemeTravelScreen과 동일) */
private val REGION_CENTER = mapOf(
    "서울" to (37.5665 to 126.9780), "부산" to (35.1796 to 129.0756),
    "대구" to (35.8714 to 128.6014), "인천" to (37.4563 to 126.7052),
    "광주" to (35.1595 to 126.8526), "대전" to (36.3504 to 127.3845),
    "울산" to (35.5384 to 129.3114), "세종" to (36.4800 to 127.2890),
    "경기" to (37.4138 to 127.5183), "강원" to (37.8854 to 127.7298),
    "충북" to (36.6357 to 127.4913), "충남" to (36.5184 to 126.8000),
    "전북" to (35.7175 to 127.1530), "전남" to (34.8161 to 126.4629),
    "경북" to (36.4919 to 128.8889), "경남" to (35.4606 to 128.2132),
    "제주" to (33.4996 to 126.5312)
)

/** 내부 전용 아이템 모델(이름 충돌 방지용) */
private data class RecoItem(
    val id: Int,
    val title: String,
    val desc: String,
    val emoji: String,
    val color: Color,
    val lat: Double,
    val lon: Double
)

/** group/region 안전값 보정 */
private fun normalizeGroup(raw: String): String = when (raw) {
    "가족", "연인", "친구", "단체", "혼자" -> raw
    else -> "가족"
}
private fun normalizeRegion(raw: String): String =
    if (REGION_CENTER.keys.contains(raw)) raw else "서울"

/** 더미 데이터 생성기 */
private fun fakeItems(themeKey: String, group: String, region: String, count: Int): List<RecoItem> {
    val baseTitles: List<String> = when (themeKey) {
        "history" -> when (group) {
            "가족" -> listOf("고궁 & 체험학습", "역사박물관+키즈존", "향교 전통놀이", "성곽 산책")
            "연인" -> listOf("고즈넉한 서원길", "야경 성곽 투어", "북촌 골목 사진", "전통찻집")
            "친구" -> listOf("근대골목 투어", "성벽 파노라마", "미술관+카페", "야시장")
            "단체" -> listOf("해설 동행", "대형 박물관", "버스 투어", "유적지 라인업")
            else   -> listOf("혼자 고궁 산책", "작은 박물관", "문화서점", "옛길 산책")
        }
        "healing" -> listOf("숲치유 산책", "온천 휴식", "호숫가 피크닉", "별보기")
        "bike"    -> listOf("하천 자전거길", "호수 순환", "해안 라이딩", "업힐 도전")
        else      -> listOf("대표 명소", "야경 포인트", "카페 라운딩", "포토 스팟")
    }
    val emojis = listOf("🏞️","🏯","🌿","📸","🗺️","🍜","🏖️","🚴")
    val colors = listOf(
        Color(0xFFBEE3F8), Color(0xFFC6F6D5), Color(0xFFFFE6A7), Color(0xFFFFD5E5),
        Color(0xFFE9D5FF), Color(0xFFFFF3BF), Color(0xFFD1FAE5), Color(0xFFE0E7FF)
    )
    val center = REGION_CENTER[region] ?: (37.5665 to 126.9780)

    return List(count) { i ->
        val jitter = 0.01 * ((i % 5) - 2)
        RecoItem(
            id = i + 1,
            title = baseTitles[i % baseTitles.size] + " ${i + 1}",
            desc  = "2~3시간 코스 · 초보 추천",
            emoji = emojis[i % emojis.size],
            color = colors[i % colors.size],
            lat = center.first + jitter,
            lon = center.second + jitter
        )
    }
}

@Composable
fun RecommendFullScreen(
    navController: NavHostController,
    themeKey: String,
    group: String,
    region: String
) {
    // 파라미터 안전화
    val safeGroup = remember(group) { normalizeGroup(group) }
    val safeRegion = remember(region) { normalizeRegion(region) }
    val center = REGION_CENTER[safeRegion] ?: (37.5665 to 126.9780)

    // 더미 데이터
    val items = remember(themeKey, safeGroup, safeRegion) {
        fakeItems(themeKey, safeGroup, safeRegion, count = 50)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$safeRegion · 추천 코스 전체") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        navController.navigate(
                            Routes.categoryPlaceListRoute(
                                category = "lodging",
                                title = "숙소 찾기",
                                themeTitle = "$safeRegion 추천 코스 근처",
                                lat = center.first, lng = center.second, radiusKm = 3.0
                            )
                        )
                    }) { Text("숙소 넓게 보기") }

                    TextButton(onClick = {
                        navController.navigate(
                            Routes.categoryPlaceListRoute(
                                category = "restaurant",
                                title = "식당 찾기",
                                themeTitle = "$safeRegion 추천 코스 근처",
                                lat = center.first, lng = center.second, radiusKm = 3.0
                            )
                        )
                    }) { Text("식당 넓게 보기") }
                }
            )
        }
    ) { inner ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "추천 코스가 없습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    RecoCard(item = item) {
                        navController.navigate(
                            Routes.placeDetailRoute(
                                placeId   = "${themeKey}_${item.id}",
                                companion = safeGroup,
                                title     = item.title,
                                lat       = item.lat,
                                lon       = item.lon
                            )
                        )
                    }
                }
            }
        }
    }
}

/* 카드 */
@Composable
private fun RecoCard(item: RecoItem, onClick: () -> Unit) {
    val bg = item.color.copy(alpha = 0.25f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(bg, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(Modifier.align(Alignment.TopStart)) {
            Text(item.emoji, style = MaterialTheme.typography.headlineSmall)
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.desc,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
