// app/src/main/java/com/gamsung2/ui/components/CalendarWithWeather.kt
package com.gamsung2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/* ▼▼ 이 파일에서만 쓰는 경량 모델(외부 util 의존 제거) ▼▼ */
private enum class WxType { SUNNY, CLOUDY, RAIN, SNOW, OTHER }
private data class DayWx(
    val date: LocalDate,
    val type: WxType,
    val min: Int,
    val max: Int
)
/* ▲▲------------------------------------------------------▲▲ */

@Composable
fun CalendarWithWeather(
    modifier: Modifier = Modifier,
    days: Int = 7,
    holidays: Map<LocalDate, String> = emptyMap(),
    eventsCount: Map<LocalDate, Int> = emptyMap(),
    onDayClick: (LocalDate) -> Unit = {}
) {
    var items by remember { mutableStateOf(emptyList<DayWx>()) }

    // 더미 데이터(오늘부터 days일 분량)
    LaunchedEffect(days) {
        val start = LocalDate.now()
        val icons = listOf(WxType.SUNNY, WxType.CLOUDY, WxType.RAIN, WxType.SNOW, WxType.CLOUDY, WxType.SUNNY, WxType.OTHER)
        items = (0 until days).map { i ->
            val d = start.plusDays(i.toLong())
            val seed = d.toEpochDay().toInt()
            val min = 8 + (seed % 7)
            val max = min + 4 + ((seed / 3) % 4)
            DayWx(date = d, type = icons[i % icons.size], min = min, max = max)
        }
    }

    Column(modifier) {
        Text("이번 주", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.date }) { d ->
                DayCell(
                    data = d,
                    holidayLabel = holidays[d.date],
                    eventCount = eventsCount[d.date] ?: 0,
                    onClick = { onDayClick(d.date) }
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    data: DayWx,
    holidayLabel: String?,
    eventCount: Int,
    onClick: () -> Unit
) {
    ElevatedCard(onClick = onClick, modifier = Modifier.size(52.dp)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("${data.date.dayOfMonth}", style = MaterialTheme.typography.labelMedium)

            Icon(
                imageVector = when (data.type) {
                    WxType.SUNNY -> Icons.Filled.WbSunny
                    WxType.CLOUDY -> Icons.Filled.Cloud
                    WxType.RAIN -> Icons.Filled.WaterDrop
                    WxType.SNOW -> Icons.Filled.AcUnit
                    WxType.OTHER -> Icons.Filled.HelpOutline
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            if (!holidayLabel.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    holidayLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (eventCount > 0) {
                Spacer(Modifier.height(2.dp))
                AssistChip(
                    onClick = onClick,
                    label = { Text("일정 $eventCount") }
                )
            }
        }
    }
}
