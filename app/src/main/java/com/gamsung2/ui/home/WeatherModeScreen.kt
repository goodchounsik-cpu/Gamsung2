// app/src/main/java/com/gamsung2/ui/home/WeeklyRangeStrip.kt
package com.gamsung2.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    val container = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    else MaterialTheme.colorScheme.surface

    val outline: BorderStroke? =
        if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        else null

    Surface(
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        border = outline,
        color = container,
        modifier = modifier.padding(horizontal = 2.dp).height(90.dp).clickable { onClick() }
    ) {
        Column(
            Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.KOREAN),
                    style = MaterialTheme.typography.labelSmall, color = dayColor
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
                    maxLines = 1, overflow = TextOverflow.Clip, softWrap = false
                )
            }
            when {
                !holidayLabel.isNullOrBlank() -> Text(holidayLabel, style = MaterialTheme.typography.labelSmall, color = sundayRed, maxLines = 1, overflow = TextOverflow.Ellipsis)
                eventCount > 0 -> Text("일정 $eventCount", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
