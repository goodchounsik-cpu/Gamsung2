@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gamsung2.data.Holiday
import com.gamsung2.data.HolidayCategory
import com.gamsung2.data.HolidayIcsRepository
import com.gamsung2.nav.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthCalendarScreen(
    navController: NavHostController? = null,
    initialDate: LocalDate = LocalDate.now()
) {
    var currentYm by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selected by remember { mutableStateOf(initialDate) }

    // 연도 바뀔 때만 휴일 재로딩
    val repo = remember { HolidayIcsRepository() }
    var loading by remember(currentYm.year) { mutableStateOf(true) }
    val holidays by produceState<Map<LocalDate, Holiday>>(initialValue = emptyMap(), currentYm.year) {
        loading = true
        value = withContext(Dispatchers.IO) {
            runCatching { repo.getHolidays(currentYm.year) }.getOrElse { emptyMap() }
        }
        loading = false
    }

    val titleFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM") }
    val monthCells = remember(currentYm) { buildMonthCells(currentYm) }
    val snack = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("월 달력") },
                navigationIcon = {
                    navController?.let {
                        IconButton(onClick = { it.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            navController?.let {
                ExtendedFloatingActionButton(
                    onClick = { it.navigate(Routes.eventEditorRoute(date = selected.toString())) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("일정 추가") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 월 이동 헤더
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { currentYm = currentYm.minusMonths(1) }) { Text("〈") }
                    Text(currentYm.format(titleFormatter), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { currentYm = currentYm.plusMonths(1) }) { Text("〉") }
                }

                Spacer(Modifier.height(8.dp))

                // 요일 헤더 (12.sp → 14.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("일","월","화","수","목","금","토").forEachIndexed { idx, d ->
                        Text(
                            text = d,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = when (idx) {
                                0 -> Color(0xFFD32F2F)
                                6 -> Color(0xFF1976D2)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 월 그리드
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(monthCells) { cell ->
                        val hol = holidays[cell.date]
                        DayCell(
                            date = cell.date,
                            inThisMonth = cell.inCurrentMonth,
                            selected = cell.date == selected,
                            holiday = hol,
                            onClick = { selected = cell.date }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                val label = holidays[selected]?.name?.let { "공휴/기념일: $it" } ?: "선택일: $selected"
                Text(label, fontSize = 14.sp)
            }

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(32.dp)
                )
            }
        }
    }
}

/* ─────────────── helpers ─────────────── */

private data class DayCellUi(
    val date: LocalDate,
    val inCurrentMonth: Boolean
)

private fun buildMonthCells(ym: YearMonth): List<DayCellUi> {
    val first = ym.atDay(1)
    val firstDow = first.dayOfWeek.sundayStartIndex()
    val start = first.minusDays(firstDow.toLong())
    return (0 until 35).map { i ->
        val d = start.plusDays(i.toLong())
        DayCellUi(date = d, inCurrentMonth = (d.month == ym.month))
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inThisMonth: Boolean,
    selected: Boolean,
    holiday: Holiday?,
    onClick: () -> Unit
) {
    val dow = date.dayOfWeek.sundayStartIndex()

    val dayColor = when {
        holiday?.category == HolidayCategory.PUBLIC_HOLIDAY -> Color(0xFFD32F2F)
        holiday?.category == HolidayCategory.NATIONAL_DAY   -> Color(0xFFD32F2F)
        dow == 0 -> Color(0xFFD32F2F)
        dow == 6 -> Color(0xFF1976D2)
        else     -> MaterialTheme.colorScheme.onSurface
    }

    val numberColor =
        if (inThisMonth) dayColor
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 날짜 숫자 (14.sp → 16.sp)
            Text(
                text = date.dayOfMonth.toString(),
                color = numberColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            if (holiday != null) {
                Spacer(Modifier.height(2.dp))
                val dotColor = when (holiday.category) {
                    HolidayCategory.PUBLIC_HOLIDAY -> MaterialTheme.colorScheme.primary
                    HolidayCategory.NATIONAL_DAY   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    HolidayCategory.COMMEMORATION  -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                }
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

/** Sun=0 ~ Sat=6 인덱스 */
private fun DayOfWeek.sundayStartIndex(): Int =
    when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
