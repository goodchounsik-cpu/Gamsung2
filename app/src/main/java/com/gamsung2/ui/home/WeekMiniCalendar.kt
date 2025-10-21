package com.gamsung2.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate

/** 날짜별 예보(이모지 + 최저/최고) */
data class WeekDayForecast(
    val emoji: String?,   // 예: "🌧️" (null이면 생략)
    val min: Int?,        // null이면 생략
    val max: Int?
)

@Composable
fun WeekMiniCalendar(
    baseDate: LocalDate = LocalDate.now(),
    selectedDate: LocalDate = LocalDate.now(),
    holidays: Map<LocalDate, String> = emptyMap(),
    forecastOf: (LocalDate) -> WeekDayForecast? = { null },
    onPrevWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {},
    onSelect: (LocalDate) -> Unit = {}
) {
    val startOfWeek = remember(baseDate) {
        baseDate.minusDays((baseDate.dayOfWeek.value % 7).toLong())
    }
    val days = remember(startOfWeek) { (0 until 7).map { startOfWeek.plusDays(it.toLong()) } }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("◀", modifier = Modifier.clickable { onPrevWeek() }.padding(8.dp))
        Text(
            "${startOfWeek.monthValue}/${startOfWeek.dayOfMonth} ~ ${days.last().monthValue}/${days.last().dayOfMonth}",
            style = MaterialTheme.typography.labelMedium
        )
        Text("▶", modifier = Modifier.clickable { onNextWeek() }.padding(8.dp))
    }

    Spacer(Modifier.height(4.dp))

    // 옆칸 간격은 1dp로 촘촘하게
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        days.forEach { d ->
            val isSelected = d == selectedDate
            val isToday = d == LocalDate.now()
            val isSun = d.dayOfWeek == DayOfWeek.SUNDAY
            val isSat = d.dayOfWeek == DayOfWeek.SATURDAY
            val holidayName = holidays[d]
            val isHoliday = holidayName != null
            val fc = forecastOf(d)

            val textColor = when {
                isHoliday || isSun -> MaterialTheme.colorScheme.error
                isSat -> Color(0xFF1976D2)
                else -> MaterialTheme.colorScheme.onSurface
            }
            val bg = when {
                holidayName?.let { it.contains("설") || it.contains("추석") } == true ->
                    Color(0xFFFFF3E0) // 명절 3일 강조
                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else -> Color.Transparent
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(if (fc == null) 64.dp else 106.dp) // 내용 늘어난 만큼 여유
                    .background(bg, MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onSelect(d) }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1) 요일 (맨 위)
                Text(
                    weekdayKoreanShort(d.dayOfWeek),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )

                // 2) 날짜 숫자
                Text(
                    d.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )

                // 3) 날씨 이모지
                fc?.emoji?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, fontSize = 13.sp, color = textColor)
                }

                // 4) 기온 (최저°/최고°)
                if (fc?.min != null && fc.max != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${fc.min}°/${fc.max}°",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = textColor,
                        letterSpacing = (-0.15).sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun weekdayKoreanShort(dow: DayOfWeek): String = when (dow) {
    DayOfWeek.SUNDAY -> "일"
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
}
