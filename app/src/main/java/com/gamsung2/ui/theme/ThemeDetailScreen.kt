// app/src/main/java/com/gamsung2/ui/theme/ThemeDetailScreen.kt
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@file:Suppress("SpellCheckingInspection", "UNUSED_PARAMETER")

package com.gamsung2.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamsung2.util.WeekDayForecastUi
import com.gamsung2.ui.home.WeeklyRangeStrip as HomeWeekStrip
import com.gamsung2.util.HolidayProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

/* 일요일 시작 주 계산 */
private fun LocalDate.startOfWeekSunday(): LocalDate =
    this.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

@Composable
fun ThemeDetailScreen(
    title: String,
    themeKey: String,
    onBack: () -> Unit = {},
    onCompanionClick: (String) -> Unit = {},
    onCardClick: (placeId: String, placeTitle: String, companion: String) -> Unit = { _, _, _ -> },
    onFindLodging: (themeTitle: String) -> Unit = {},
    onFindRestaurant: (themeTitle: String) -> Unit = {},
    onSearchSimilar: (String?) -> Unit = {} // ✅ 통합검색 진입 콜백
) {
    /* 동행 선택 */
    val companions = listOf("가족", "연인", "친구", "단체", "혼자")
    var selectedCompanion by rememberSaveable(themeKey) { mutableStateOf(companions.first()) }
    val recommendations by remember(selectedCompanion) { mutableStateOf(getRecsFor(selectedCompanion)) }

    /* 핵심 상태 */
    val today = remember { LocalDate.now() }
    var baseDate by rememberSaveable(themeKey) { mutableStateOf(today) }
    var selectedDate by rememberSaveable(themeKey) { mutableStateOf(today) }

    /* 날씨(더미) */
    val weekWeather = remember(baseDate) { buildWeekWeatherForDtl(baseDate) }
    val todayWeather = remember { buildWeekWeatherForDtl(today) }

    /* 즐겨찾기 상태 & 필터 */
    val favIds = rememberSaveable(themeKey) { mutableStateListOf<String>() }
    var favOnly by rememberSaveable(themeKey) { mutableStateOf(false) }

    /* 카드 선택/페이저 */
    val selectedIds = remember { mutableStateListOf<String>() }   // ✅ 고침
    val baseList = remember(recommendations, favOnly, favIds) {
        if (favOnly) recommendations.filter { it.id in favIds } else recommendations
    }
    val pageCount = remember(baseList) { ceil(baseList.size / 4f).toInt().coerceAtLeast(1) }
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (title.isNotBlank()) title else "추천 테마") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { onSearchSimilar(title.ifBlank { null }) }) {
                        Icon(Icons.Filled.Search, contentDescription = "검색")
                    }
                }
            )
        },
        bottomBar = {
            val enabled = selectedIds.isNotEmpty()
            Surface(tonalElevation = 2.dp) {
                Button(
                    onClick = {
                        val first = selectedIds.firstOrNull() ?: return@Button
                        val placeTitle = recommendations.firstOrNull { it.id == first }?.title ?: "선택한 코스"
                        onCardClick(first, placeTitle, selectedCompanion)
                    },
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                ) { Text(if (enabled) "선택한 코스 보기 (${selectedIds.size})" else "코스를 선택하세요") }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            Text("누구와 함께 가시나요?")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                companions.forEach { who ->
                    FilterChip(
                        selected = (who == selectedCompanion),
                        onClick = { selectedCompanion = who; selectedIds.clear(); onCompanionClick(who) },
                        label = { Text(who) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            /* 휴일/이벤트 맵 */
            val weekStart = remember(baseDate) { baseDate.startOfWeekSunday() }
            val holidaysForWeek: Map<LocalDate, String> =
                remember(weekStart) { HolidayProvider.getHolidaysInRange(weekStart, 7) }
            val eventsCountForWeek: Map<LocalDate, Int> = remember(weekStart) { emptyMap() }

            /* 주간 스트립 */
            HomeWeekStrip(
                baseDate = baseDate,
                forecastOf = { d: LocalDate ->
                    weekWeather.find { it.localDate == d }?.let { w ->
                        WeekDayForecastUi(emoji = w.icon, min = w.minTemp, max = w.maxTemp)
                    }
                },
                onPrevWeek = { baseDate = baseDate.minusWeeks(1) },
                onNextWeek = { baseDate = baseDate.plusWeeks(1) },
                onSelectDate = { d: LocalDate -> selectedDate = d },
                selected = selectedDate,
                holidays = holidaysForWeek,
                eventsCount = eventsCountForWeek,
                showRangeHeader = true,
                showTodaySummary = true,
                onJumpToToday = {
                    selectedDate = today
                    baseDate = today
                },
                todayForecast = {
                    todayWeather.find { it.localDate == today }?.let { w ->
                        WeekDayForecastUi(emoji = w.icon, min = w.minTemp, max = w.maxTemp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(6.dp)
            )

            Spacer(Modifier.height(16.dp))

            /* 상단 컨트롤: 즐겨찾기 필터 + 액션 버튼 */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = favOnly,
                        onClick = { favOnly = !favOnly },
                        label = { Text("즐겨찾기만 보기") },
                        leadingIcon = {
                            Icon(
                                if (favOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = null
                            )
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("즐겨찾기 ${favIds.size}") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { onFindLodging(title) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Hotel, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("숙소 찾기", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { onFindRestaurant(title) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Restaurant, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("식당 찾기", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (baseList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "즐겨찾기한 코스가 없습니다.\n카드의 하트를 눌러 추가해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val slice = baseList.drop(page * 4).take(4)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(items = slice, key = { it.id }) { rec ->
                            val isSelected = rec.id in selectedIds
                            val isFav = rec.id in favIds
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color(0xFFE0F7FA))
                                    .then(if (isSelected) Modifier.background(Color(0xFFEDE7F6)) else Modifier)
                                    .clickable {
                                        if (isSelected) selectedIds.remove(rec.id) else selectedIds.add(rec.id)
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    rec.title,
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )

                                if (isSelected) {
                                    Row(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                IconToggleButton(
                                    checked = isFav,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (rec.id !in favIds) favIds.add(rec.id)
                                        } else {
                                            favIds.remove(rec.id)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = if (isFav) "즐겨찾기 해제" else "즐겨찾기",
                                        tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ----------------------- 샘플 데이터/더미 ----------------------- */
private data class RecItem(val id: String, val title: String)
private fun getRecsFor(companion: String): List<RecItem> {
    val base = listOf(
        "한옥마을","고궁 야간개장","성곽 트레킹","미술관","전통시장","역사박물관",
        "야시장 먹거리","강변 야경","수목원","야외공연","재래시장 맛집","도보투어"
    )
    val offset = when (companion) { "가족" -> 0; "연인" -> 2; "친구" -> 4; "단체" -> 6; else -> 8 }
    val rotated = base.drop(offset) + base.take(offset)
    return rotated.mapIndexed { i, t -> RecItem("${companion}_$i", t) }
}

private data class ThemeDtlWeatherDay(val localDate: LocalDate, val minTemp: Int, val maxTemp: Int, val icon: String)
private fun buildWeekWeatherForDtl(baseDate: LocalDate): List<ThemeDtlWeatherDay> {
    val sunday = baseDate.startOfWeekSunday()
    val icons = listOf("❄️","☁️","🌧️","❄️","🌞","☁️","🌧️")
    return (0..6).map { i ->
        val d = sunday.plusDays(i.toLong())
        val seed = d.toEpochDay().toInt()
        val min = 8 + (seed % 7)
        val max = min + 5 + ((seed / 3) % 4)
        ThemeDtlWeatherDay(d, min, max, icons[i % icons.size])
    }
}
