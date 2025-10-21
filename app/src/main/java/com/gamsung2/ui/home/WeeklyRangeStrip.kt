package com.gamsung2.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamsung2.util.WeekDayForecastUi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeeklyRangeStrip(
    baseDate: LocalDate,
    forecastOf: (LocalDate) -> WeekDayForecastUi?,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    selected: LocalDate,
    holidays: Map<LocalDate, String> = emptyMap(),
    eventsCount: Map<LocalDate, Int> = emptyMap(),
    showRangeHeader: Boolean = true,
    showTodaySummary: Boolean = true,
    onJumpToToday: () -> Unit,                    // ✅ 널 아닌 전용 콜백
    todayForecast: (() -> WeekDayForecastUi?)? = null,
    today: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier
) {
    val weekStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
    val todayFc = todayForecast?.invoke() ?: forecastOf(today)

    Column(modifier) {
        if (showRangeHeader) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${weekStart.format(DateTimeFormatter.ofPattern("M/d"))} ~ " +
                            weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("M/d")),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onPrevWeek) { Text("이전") }
                TextButton(onClick = {
                    onSelectDate(today)           // 선택 하이라이트도 갱신
                    onJumpToToday()               // ✅ 한 곳만 호출
                }) { Text("오늘") }
                TextButton(onClick = onNextWeek) { Text("다음") }
            }

            if (showTodaySummary) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectDate(today)
                            onJumpToToday()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("오늘", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(8.dp))
                        Text("${today.monthValue} / ${today.dayOfMonth}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            today.dayOfWeek.getDisplayName(
                                java.time.format.TextStyle.FULL, java.util.Locale.KOREAN
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(todayFc?.emoji?.takeIf { it.isNotBlank() } ?: "•",
                            style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${todayFc?.min?.let { "$it°" } ?: "—"} / ${todayFc?.max?.let { "$it°" } ?: "—"}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { d ->
                DayCell(
                    date = d,
                    forecast = forecastOf(d),
                    holidayLabel = holidays[d],
                    eventCount = eventsCount[d] ?: 0,
                    selected = d == selected,
                    isToday = d == today,
                    onClick = {
                        onSelectDate(d)
                        if (d == today) onJumpToToday()    // 오늘 셀도 동일 루트
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    forecast: WeekDayForecastUi?,
    holidayLabel: String?,
    eventCount: Int,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sundayRed = Color(0xFFD32F2F)
    val saturdayBlue = Color(0xFF1976D2)

    val dayColor: Color = when {
        !holidayLabel.isNullOrBlank() || date.dayOfWeek == DayOfWeek.SUNDAY -> sundayRed
        date.dayOfWeek == DayOfWeek.SATURDAY -> saturdayBlue
        else -> MaterialTheme.colorScheme.onSurface
    }

    val container =
        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surface

    val outline: BorderStroke? =
        if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        else null

    Surface(
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        border = outline,
        color = container,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(90.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.NARROW, java.util.Locale.KOREAN
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = dayColor
                )
                Text(
                    "${date.dayOfMonth}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = dayColor
                )
            }
            forecast?.emoji?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (forecast?.min != null || forecast?.max != null) {
                Text(
                    "${forecast.min?.let { "$it°" } ?: "—"} / ${forecast.max?.let { "$it°" } ?: "—"}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    softWrap = false
                )
            }
            when {
                !holidayLabel.isNullOrBlank() ->
                    Text(holidayLabel, style = MaterialTheme.typography.labelSmall, color = sundayRed,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                eventCount > 0 ->
                    Text("일정 $eventCount", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
