@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.gamsung2.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.gamsung2.data.local.UserPrefs
import com.gamsung2.nav.Routes
import com.gamsung2.util.WeekDayForecastUi
import com.gamsung2.util.getKoreanHolidaysMap
import java.time.LocalDate

/* ===== 레이아웃 상수 ===== */
private val OUTER_HPAD = 16.dp
private val SECTION_GAP = 12.dp
private val CARD_INNER_PAD = 10.dp
private const val THEME_CARD_ASPECT = 2.9f

/* 지역 중심 좌표 (샘플) */
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

/* ---- 예보 데이터 ---- */
private data class DailyForecast(val date: LocalDate, val min: Int, val max: Int, val emoji: String)

@Composable
fun HomeScreen(
    navController: NavHostController,
    tabKey: String // "near" | "all"
) {
    val ctx = LocalContext.current

    fun safeNavigate(route: String) {
        try {
            navController.navigate(route)
        } catch (e: IllegalArgumentException) {
            val msg = "경로를 찾을 수 없습니다: \"$route\"\n${e.message ?: ""}"
            Log.e("HomeScreen", msg, e)
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        } catch (e: Throwable) {
            val msg = "이동 중 오류: ${e.message ?: e::class.java.simpleName}"
            Log.e("HomeScreen", msg, e)
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        }
    }

    val prefs = remember { UserPrefs(ctx) }

    // DataStore 초기값
    val savedRegion by prefs.lastRegionHome.collectAsState(initial = "서울")
    val savedDate   by prefs.lastDateHome.collectAsState(initial = LocalDate.now())

    // 탭별 상태 보존
    var selectedRegion by rememberSaveable(tabKey) { mutableStateOf(savedRegion) }
    var baseDate      by rememberSaveable(tabKey) { mutableStateOf(savedDate) }
    var selectedDate  by rememberSaveable(tabKey) { mutableStateOf(savedDate) }
    var refreshTick   by remember { mutableIntStateOf(0) }

    // 모드/지역/날짜 저장
    LaunchedEffect(tabKey)         { prefs.setHomeMode(if (tabKey == "all") "Nationwide" else "Nearby") }
    LaunchedEffect(selectedRegion) { prefs.setHomeRegion(selectedRegion) }
    LaunchedEffect(selectedDate)   { prefs.setHomeDate(selectedDate) }

    val regions = remember {
        listOf("서울","부산","대구","인천","광주","대전","울산","세종","경기","강원","충북","충남","전북","전남","경북","경남","제주")
    }

    // 날짜/휴일/예보(더미)
    val (lat, lon)  = (REGION_CENTER[selectedRegion] ?: (37.5665 to 126.9780))
    val weekStart   = remember(baseDate) { baseDate.minusDays((baseDate.dayOfWeek.value % 7).toLong()) }
    val holidaysMap = remember(baseDate.year) { getKoreanHolidaysMap(baseDate.year) }
    val forecasts = remember(baseDate, selectedRegion, refreshTick) { buildFakeForecast(lat, lon, weekStart) }
    val forecastOf: (LocalDate) -> WeekDayForecastUi? = { date ->
        forecasts.firstOrNull { it.date == date }?.let { WeekDayForecastUi(it.emoji, it.min, it.max) }
    }
    val todayForecast: () -> WeekDayForecastUi? = {
        forecasts.firstOrNull { it.date == LocalDate.now() }?.let { WeekDayForecastUi(it.emoji, it.min, it.max) }
    }

    var showRegionPickerFor by remember { mutableStateOf<ThemeItem?>(null) }
    var showHomeRegionChooser by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
        contentPadding = PaddingValues(
            start = OUTER_HPAD, end = OUTER_HPAD, top = SECTION_GAP,
            bottom = SECTION_GAP + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        // 2) 나만의 허브
        item {
            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerDefaults.color),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "나만의 허브",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        @Composable
                        fun HubLink(label: String, onClick: () -> Unit) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onClick),
                                textAlign = TextAlign.Center
                            )
                        }

                        // ✅ 요청대로 연결
                        HubLink("나만의 여행") {
                            safeNavigate(Routes.searchRoute(query = "", bucket = "course"))
                        }
                        HubLink("나만의 버킷") {
                            safeNavigate(Routes.bucketHomeRoute())
                        }
                        HubLink("코스짜기") {
                            safeNavigate(Routes.searchRoute(query = "", bucket = "course"))
                        }
                    }
                }
            }
        }

        // 3) 주변/전국 토글
        item {
            ModeBar(
                isNearby = tabKey == "near",
                onClickNearby = { if (tabKey != "near") safeNavigate(Routes.HOME_NEAR) },
                onClickNationwide = { if (tabKey != "all")  safeNavigate(Routes.HOME_ALL) }
            )
        }

        // 4) 테마
        item {
            SectionCard(
                title = "테마",
                leading = { Icon(Icons.Filled.Category, null, Modifier.size(18.dp)) }
            ) {
                ThemeGrid(
                    items = listOf(
                        ThemeItem("history","역사여행", Icons.Filled.Category),
                        ThemeItem("healing","힐링여행", Icons.Filled.Category),
                        ThemeItem("photo","사진여행", Icons.Filled.Category),
                        ThemeItem("activity","촬영지 여행", Icons.Filled.Category),
                        ThemeItem("bike","자전거여행", Icons.AutoMirrored.Filled.DirectionsBike),
                        ThemeItem("festival","축제/행사", Icons.Filled.Event),
                        ThemeItem("food","먹거리여행", Icons.Filled.Category),
                        ThemeItem("drive","드라이브코스", Icons.Filled.Category),
                    ),
                    onClick = { item ->
                        if (tabKey == "near") {
                            safeNavigate("${Routes.THEME_TRAVEL}?key=${item.key}&title=${item.title}")
                        } else {
                            showRegionPickerFor = item
                        }
                    }
                )
            }
        }

        // 5) 주간 달력
        item {
            SectionCard(
                title = "주간 달력",
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { showHomeRegionChooser = true },
                            label = { Text("지역: $selectedRegion", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(Icons.Filled.Place, null, Modifier.size(14.dp)) }
                        )
                        IconButton(onClick = { refreshTick++ }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "새로고침")
                        }
                        TextButton(onClick = { safeNavigate(Routes.calendarMonthRoute()) }) {
                            Text("월 달력", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            ) {
                WeeklyRangeStrip(
                    baseDate = baseDate,
                    forecastOf = forecastOf,
                    onPrevWeek = { baseDate = baseDate.minusDays(7) },
                    onNextWeek = { baseDate = baseDate.plusDays(7) },
                    onSelectDate = { d -> selectedDate = d },
                    selected = selectedDate,
                    holidays = holidaysMap,
                    eventsCount = emptyMap(),
                    showRangeHeader = true,
                    showTodaySummary = true,
                    onJumpToToday = {
                        baseDate = LocalDate.now()
                        selectedDate = LocalDate.now()
                    },
                    todayForecast = todayForecast,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .padding(4.dp)
                )
            }
        }
    }

    // 전국 모드: 테마 → 지역 선택
    if (showRegionPickerFor != null) {
        val theme = showRegionPickerFor!!
        AlertDialog(
            onDismissRequest = { showRegionPickerFor = null },
            confirmButton = { TextButton(onClick = { showRegionPickerFor = null }) { Text("닫기") } },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = { Text("지역 선택", style = MaterialTheme.typography.titleSmall) },
            text = {
                Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (listOf("전체") + listOf(
                            "서울","부산","대구","인천","광주","대전","울산","세종",
                            "경기","강원","충북","충남","전북","전남","경북","경남","제주"
                        )).forEach { region ->
                            AssistChip(
                                onClick = {
                                    showRegionPickerFor = null
                                    val title = if (region == "전체") theme.title else "$region · ${theme.title}"
                                    val regionArg = if (region == "전체") "" else region
                                    safeNavigate(
                                        "${Routes.THEME_TRAVEL}?key=${theme.key}" +
                                                "&title=${title}" +
                                                "&region=${regionArg}"
                                    )
                                },
                                label = { Text(text = region, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OUTER_HPAD)
        )
    }

    // 홈 주간달력: 지역 선택
    if (showHomeRegionChooser) {
        AlertDialog(
            onDismissRequest = { showHomeRegionChooser = false },
            confirmButton = { TextButton(onClick = { showHomeRegionChooser = false }) { Text("닫기") } },
            title = { Text("지역 선택", style = MaterialTheme.typography.titleSmall) },
            text = {
                Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        regions.forEach { r ->
                            AssistChip(
                                onClick = { selectedRegion = r; showHomeRegionChooser = false },
                                label = { Text(r, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        )
    }
}

/* ============================== UI pieces ============================== */

@Composable
private fun ModeBar(
    isNearby: Boolean,
    onClickNearby: () -> Unit,
    onClickNationwide: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerDefaults.color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .height(36.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentedItem(
                selected = isNearby,
                onClick = onClickNearby,
                icon = { Icon(Icons.Filled.MyLocation, null, Modifier.size(16.dp)) },
                label = "주변",
                labelSizeSp = 13.sp,
                modifier = Modifier.weight(1f)
            )
            SegmentedItem(
                selected = !isNearby,
                onClick = onClickNationwide,
                icon = { Icon(Icons.Filled.Public, null, Modifier.size(16.dp)) },
                label = "전국",
                labelSizeSp = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentedItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    labelSizeSp: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    val colors = if (selected) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.outlinedButtonColors()
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = colors,
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            icon()
            Text(label, fontSize = labelSizeSp)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(CARD_INNER_PAD)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (leading != null) leading()
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                if (trailing != null) trailing()
            }
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

/** 테마 그리드(2열) */
private data class ThemeItem(val key: String, val title: String, val icon: ImageVector)

@Composable
private fun ThemeGrid(items: List<ThemeItem>, onClick: (ThemeItem) -> Unit) {
    val gap = 12.dp
    val iconSize = 24.dp
    val cardShape = RoundedCornerShape(14.dp)

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val contentWidth = screenWidthDp - OUTER_HPAD * 2
    val cardWidth = (contentWidth - gap) / 2
    val cardHeight = cardWidth / THEME_CARD_ASPECT

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { item ->
                    ElevatedCard(
                        onClick = { onClick(item) },
                        shape = cardShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(cardHeight)
                    ) {
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.icon, null, Modifier.size(iconSize))
                            Spacer(Modifier.size(10.dp))
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f).height(cardHeight))
            }
        }
    }
}

/* ---- 더미 예보 생성기 ---- */
private fun buildFakeForecast(lat: Double, lon: Double, weekStart: LocalDate): List<DailyForecast> {
    val emojis = listOf("☀️","☁️","🌧️","⛅","❄️","🌦️","🌤️")
    val baseSeed = ((lat * 10_000).toInt() xor (lon * 10_000).toInt())
    return (0..6).map { i ->
        val date = weekStart.plusDays(i.toLong())
        val daySeed = baseSeed + i * 37 + date.dayOfYear
        val min = 8 + (daySeed % 7 + 7) % 7
        val max = min + 5 + (((daySeed / 3) % 4 + 4) % 4)
        val emoji = emojis[((daySeed / 11) % emojis.size + emojis.size) % emojis.size]
        DailyForecast(date, min, max, emoji)
    }
}
