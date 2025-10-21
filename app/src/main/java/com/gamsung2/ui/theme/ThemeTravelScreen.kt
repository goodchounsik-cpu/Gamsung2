@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.gamsung2.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gamsung2.data.local.UserPrefs
import com.gamsung2.nav.Routes
import com.gamsung2.ui.home.WeeklyRangeStrip
import com.gamsung2.util.HolidayProvider
import com.gamsung2.util.WeekDayForecastUi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

/* ───────────────── 지역 좌표(샘플) ───────────────── */
private val REGION_CENTER = mapOf(
    "서울" to (37.5665 to 126.9780),
    "부산" to (35.1796 to 129.0756),
    "대구" to (35.8714 to 128.6014),
    "인천" to (37.4563 to 126.7052),
    "광주" to (35.1595 to 126.8526),
    "대전" to (36.3504 to 127.3845),
    "울산" to (35.5384 to 129.3114),
    "세종" to (36.4800 to 127.2890),
    "경기" to (37.4138 to 127.5183),
    "강원" to (37.8854 to 127.7298),
    "충북" to (36.6357 to 127.4913),
    "충남" to (36.5184 to 126.8000),
    "전북" to (35.7175 to 127.1530),
    "전남" to (34.8161 to 126.4629),
    "경북" to (36.4919 to 128.8889),
    "경남" to (35.4606 to 128.2132),
    "제주" to (33.4996 to 126.5312)
)

/* ───────────────── 더미 예보 모델/생성기 ───────────────── */
private data class DailyForecast(val date: LocalDate, val min: Int, val max: Int, val emoji: String)

private fun buildFakeForecast(lat: Double, lon: Double, weekStart: LocalDate): List<DailyForecast> {
    val emojis = listOf("☀️","☁️","🌧️","⛅","❄️","🌦️","🌤️")
    val seed = ((lat * 10_000).toInt() xor (lon * 10_000).toInt())
    return (0..6).map { i ->
        val d = weekStart.plusDays(i.toLong())
        val s = seed + i * 37 + d.dayOfYear
        val min = 8 + (s % 7 + 7) % 7
        val max = min + 5 + (((s / 3) % 4 + 4) % 4)
        val emoji = emojis[((s / 11) % emojis.size + emojis.size) % emojis.size]
        DailyForecast(d, min, max, emoji)
    }
}

/* ───────────────── 화면 ───────────────── */
@Composable
fun ThemeTravelScreen(
    navController: NavHostController,
    themeKey: String = "",
    themeTitle: String = "",
    regionName: String = "",
    contentPadding: PaddingValues = PaddingValues()
) {
    val ctx = LocalContext.current
    val prefs = remember { UserPrefs(ctx) }
    val scope = rememberCoroutineScope()

    // DataStore에서 마지막 테마/그룹/지역 복원
    val savedThemeKey by prefs.lastThemeKey.collectAsState(initial = "history")
    val savedGroup   by prefs.lastThemeGroup.collectAsState(initial = "가족")
    val savedRegion  by prefs.lastThemeRegion.collectAsState(initial = "서울")

    val resolvedThemeKey = remember(themeKey, savedThemeKey) { themeKey.ifBlank { savedThemeKey } }
    val title = themeTitle.ifBlank { "테마 여행" }

    // 동행(그룹)
    var selectedGroup by remember(savedGroup) { mutableStateOf(savedGroup) }

    // 날짜/휴일
    val today = LocalDate.now()
    var baseDate by remember { mutableStateOf(today) }
    var selectedDate by remember { mutableStateOf(today) }
    val weekStart = remember(baseDate) { baseDate.minusDays((baseDate.dayOfWeek.value % 7).toLong()) }
    val holidaysForWeek by remember(weekStart) { mutableStateOf(HolidayProvider.getHolidaysInRange(weekStart, 7)) }

    // 지역 (라우트 > 저장값 > 기본)
    val allRegions = remember {
        listOf("서울","부산","대구","인천","광주","대전","울산","세종","경기","강원","충북","충남","전북","전남","경북","경남","제주")
    }
    var region by remember(regionName, savedRegion) { mutableStateOf(regionName.ifBlank { savedRegion }) }
    var showRegionChooser by remember { mutableStateOf(false) }
    val (lat, lon) = REGION_CENTER[region] ?: (37.5665 to 126.9780)

    // 지역/주 변경 → 7일 예보 갱신(더미)
    val forecasts by produceState(initialValue = emptyList<DailyForecast>(), region, weekStart) {
        value = buildFakeForecast(lat, lon, weekStart)
    }
    val forecastOf: (LocalDate) -> WeekDayForecastUi? = { d ->
        forecasts.firstOrNull { it.date == d }?.let { WeekDayForecastUi(it.emoji, it.min, it.max) }
    }
    val todayForecast: () -> WeekDayForecastUi? = {
        forecasts.firstOrNull { it.date == today }?.let { WeekDayForecastUi(it.emoji, it.min, it.max) }
    }

    // -------------------- 추천 코스: 무한 스크롤 --------------------
    val pageSize = 8
    var page by remember(resolvedThemeKey, selectedGroup, region) { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // 데이터 버퍼
    var data by remember(resolvedThemeKey, selectedGroup, region) { mutableStateOf<List<RecommendItem>>(emptyList()) }

    // 첫 페이지 로드
    LaunchedEffect(resolvedThemeKey, selectedGroup, region) {
        isLoading = true
        page = 0
        val first = recommendationsForPaged(resolvedThemeKey, selectedGroup, region, 0, pageSize)
        data = first
        isLoading = false
    }

    // 끝 도달 감지 → 다음 페이지
    val gridState = rememberLazyGridState()
    val latestTheme by rememberUpdatedState(resolvedThemeKey)
    val latestGroup by rememberUpdatedState(selectedGroup)
    val latestRegion by rememberUpdatedState(region)

    LaunchedEffect(gridState) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible to total
        }
            .map { (last, total) -> if (total > 0 && last >= total - 3) last else -1 }
            .filter { it >= 0 }
            .distinctUntilChanged()
            .collectLatest {
                if (!isLoading) {
                    isLoading = true
                    page += 1
                    val more = recommendationsForPaged(
                        latestTheme, latestGroup, latestRegion, page, pageSize
                    )
                    data = buildList(data.size + more.size) {
                        addAll(data); addAll(more)
                    }
                    isLoading = false
                }
            }
    }
    // --------------------------------------------------------------

    // 상태 변경 저장
    LaunchedEffect(resolvedThemeKey) { scope.launch { prefs.setThemeKey(resolvedThemeKey) } }
    LaunchedEffect(selectedGroup)    { scope.launch { prefs.setThemeGroup(selectedGroup) } }
    LaunchedEffect(region)           { scope.launch { prefs.setThemeRegion(region) } }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /* 헤더 */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            AssistChip(
                onClick = { showRegionChooser = true },
                label = { Text("지역: $region", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = { Icon(Icons.Filled.Place, null) }
            )
        }

        /* 동행 */
        Text("누구와 함께 가시나요?", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 5
        ) {
            listOf("가족", "연인", "친구", "단체", "혼자").forEach { label ->
                FilterChip(
                    selected = selectedGroup == label,
                    onClick = { selectedGroup = label },
                    label = { Text(label) }
                )
            }
        }

        HorizontalDivider()

        /* 주간 스트립 */
        WeeklyRangeStrip(
            baseDate = baseDate,
            forecastOf = forecastOf,
            onPrevWeek = { baseDate = baseDate.minusDays(7) },
            onNextWeek = { baseDate = baseDate.plusDays(7) },
            onSelectDate = { d -> selectedDate = d },
            selected = selectedDate,
            holidays = holidaysForWeek,
            eventsCount = emptyMap(),
            showRangeHeader = true,
            showTodaySummary = true,
            onJumpToToday = { baseDate = today; selectedDate = today },
            todayForecast = todayForecast,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(6.dp)
        )

        /* 추천 코스 (제목 + 넓게 보기) */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("추천 코스", style = MaterialTheme.typography.titleMedium)
            TextButton(
                onClick = {
                    navController.navigate(
                        Routes.recoFullRoute(
                            key = resolvedThemeKey,
                            group = selectedGroup,
                            region = region
                        )
                    )
                }
            ) { Text("넓게 보기") }
        }

        Box(Modifier.fillMaxWidth().weight(1f, fill = true)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(data, key = { it.id }) { item ->
                    RecommendCard(
                        item = item,
                        onClick = {
                            navController.navigate(
                                Routes.placeDetailRoute(
                                    placeId = "${resolvedThemeKey}_${item.id}",
                                    companion = selectedGroup,
                                    title = item.title,
                                    lat = item.lat,
                                    lon = item.lon
                                )
                            )
                        }
                    )
                }

                if (isLoading && data.isNotEmpty()) {
                    // 리스트 맨 아래에 '로딩 더보기'
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // 첫 페이지 로딩 오버레이
            if (isLoading && data.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        Text(
            "코스를 탭하면 해당 장소 상세로 이동합니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    /* 지역 선택 다이얼로그 */
    if (showRegionChooser) {
        AlertDialog(
            onDismissRequest = { showRegionChooser = false },
            confirmButton = { TextButton(onClick = { showRegionChooser = false }) { Text("닫기") } },
            title = { Text("지역 선택", style = MaterialTheme.typography.titleSmall) },
            text = {
                Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allRegions.forEach { r ->
                            AssistChip(
                                onClick = { region = r; showRegionChooser = false },
                                label = { Text(r, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        )
    }
}

/* ───────────────── 추천 카드/데이터 ───────────────── */

private data class RecommendItem(
    val id: Int,
    val title: String,
    val desc: String,
    val emoji: String,
    val color: Color,
    val lat: Double,
    val lon: Double
)

@Composable
private fun RecommendCard(item: RecommendItem, onClick: () -> Unit) {
    val bg = item.color.copy(alpha = 0.25f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.align(Alignment.TopStart)) {
            Text(item.emoji, style = MaterialTheme.typography.headlineSmall)
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.desc, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

/* 페이징용 추천 생성기 */
private fun recommendationsForPaged(
    themeKey: String, group: String, region: String, page: Int, pageSize: Int
): List<RecommendItem> {
    val baseTitles: List<String> = when (themeKey) {
        "history" -> when (group) {
            "가족" -> listOf("고궁 & 체험학습", "역사박물관+키즈존", "향교 전통놀이", "성곽 산책(쉬운 코스)")
            "연인" -> listOf("고즈넉한 서원길", "야경 성곽 투어", "북촌 골목 사진", "전통찻집 데이트")
            "친구" -> listOf("근대골목 투어", "성벽 파노라마", "미술관+카페", "야시장 스냅샷")
            "단체" -> listOf("해설사 동행 코스", "대형 박물관 코스", "버스 투어 묶음", "유적지 라인업")
            else   -> listOf("혼자 고궁 산책", "작은 박물관 탐방", "문화서점 라운딩", "옛길 기록 산책")
        }
        "healing" -> listOf("숲치유 산책", "온천 휴식", "호숫가 피크닉", "별보기 캠프")
        "bike"    -> listOf("하천 자전거길", "호수 순환", "해안 라이딩", "업힐 도전")
        else      -> listOf("대표 명소 묶음", "야경 포인트", "카페 라운딩", "포토 스팟")
    }
    val emojis = listOf("🏞️", "🏯", "🌿", "📸", "🗺️", "🍜", "🏖️", "🚴")
    val colors = listOf(
        Color(0xFFBEE3F8), Color(0xFFC6F6D5), Color(0xFFFFE6A7), Color(0xFFFFD5E5),
        Color(0xFFE9D5FF), Color(0xFFFFF3BF), Color(0xFFD1FAE5), Color(0xFFE0E7FF)
    )
    val center = REGION_CENTER[region] ?: (37.5665 to 126.9780)

    return List(pageSize) { i ->
        val globalIndex = page * pageSize + i
        val t = baseTitles[globalIndex % baseTitles.size] + " ${globalIndex + 1}"
        val jitter = 0.01 * ((globalIndex % 5) - 2)
        RecommendItem(
            id = globalIndex + 1,
            title = t,
            desc = when (themeKey) {
                "history" -> "해설/포토 포함 · 난이도 하"
                "healing" -> "조용·휴식 위주 · 2~3h"
                "bike"    -> "거리/난이도 맞춤 · 헬멧 필수"
                else      -> "2~3시간 코스 · 초보 추천"
            },
            emoji = emojis[globalIndex % emojis.size],
            color = colors[globalIndex % colors.size],
            lat = center.first + jitter,
            lon = center.second + jitter
        )
    }
}
