@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.place

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamsung2.ui.place.components.RatingFilter
import com.gamsung2.ui.place.components.RatingFilterRow

/**
 * 숙소 리스트 화면 (자급형/독립 버전)
 * - 기존 공용 enum/컴포넌트와 충돌을 피하기 위해 Lodg* 접두어로 분리
 * - Repository 연동 전까지 더미 데이터 사용
 * - 평점/거리/타입 필터 적용
 */
@Composable
fun LodgingListScreen() {
    // 내부 상태
    var rating: RatingFilter by remember { mutableStateOf(RatingFilter.All) }
    var selDistance: LodgDistance by remember { mutableStateOf(LodgDistance.Km3) }
    var selType: LodgType by remember { mutableStateOf(LodgType.All) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // (1) 거리
        Text(text = "거리", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LodgDistanceChipsRow(
            selected = selDistance,
            onChange = { new -> selDistance = new }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // (2) 숙소 타입
        Text(text = "숙소 타입", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LodgTypeChipsRow(
            selected = selType,
            onChange = { new -> selType = new }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // (3) 평점 (공용 컴포넌트)
        Text(text = "평점", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        RatingFilterRow(
            selected = rating,
            onChange = { new -> rating = new }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // (4) 선택 요약
        LodgSelectedSummaryBar(
            distance = selDistance.label,
            type = selType.label
        )

        Spacer(modifier = Modifier.height(12.dp))

        // (5) 그리드
        LodgGrid(
            minRating = rating.min,
            distance = selDistance,
            type = selType
        )
    }
}

/* ---------------------------- Chips & Models (Lodg*) ---------------------------- */

private enum class LodgDistance(val label: String, val meters: Int?) {
    M500("500m", 500),
    Km1("1km", 1_000),
    Km3("3km", 3_000),
    Km5("5km", 5_000),
    Km10("10km", 10_000)
}

@Composable
private fun LodgDistanceChipsRow(
    selected: LodgDistance,
    onChange: (LodgDistance) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LodgDistance.values().forEach { item ->
            FilterChip(
                selected = (item == selected),
                onClick = { onChange(item) },
                label = { Text(text = item.label) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

private enum class LodgType(val label: String) {
    All("전체"),
    Hotel("호텔"),
    Motel("모텔"),
    Pension("펜션"),
    GuestHouse("민박")
}

@Composable
private fun LodgTypeChipsRow(
    selected: LodgType,
    onChange: (LodgType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LodgType.values().forEach { item ->
            FilterChip(
                selected = (item == selected),
                onClick = { onChange(item) },
                label = { Text(text = item.label) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

/* ---------------------------- Summary Bar ---------------------------- */

@Composable
private fun LodgSelectedSummaryBar(
    distance: String,
    type: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "숙소 · 주변 $distance",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = "· $type", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/* ---------------------------- Grid (더미 데이터) ---------------------------- */

private data class LodgUi(
    val name: String,
    val type: LodgType,
    val distanceMeters: Int,
    val rating: Float?
)

private fun lodgSamples(): List<LodgUi> = listOf(
    LodgUi("숙소 #1", LodgType.Motel, 200, 4.0f),
    LodgUi("숙소 #2", LodgType.Pension, 400, 4.5f),
    LodgUi("숙소 #3", LodgType.GuestHouse, 1500, 3.5f),
    LodgUi("숙소 #4", LodgType.Hotel, 2500, 4.7f),
    LodgUi("숙소 #5", LodgType.Motel, 5200, 4.2f),
    LodgUi("숙소 #6", LodgType.Hotel, 9800, 3.9f),
)

@Composable
private fun LodgGrid(
    minRating: Float?,
    distance: LodgDistance,
    type: LodgType
) {
    val source: List<LodgUi> = remember { lodgSamples() }

    val filtered: List<LodgUi> = source.filter { item ->
        val okRating = (minRating == null) || ((item.rating ?: 0f) >= minRating)
        val okType = (type == LodgType.All) || (item.type == type)
        val okDistance = distance.meters?.let { lim -> item.distanceMeters <= lim } ?: true
        okRating && okType && okDistance
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(items = filtered, key = { it.name }) { item ->
            LodgItemCard(item = item)
        }
    }
}

@Composable
private fun LodgItemCard(item: LodgUi) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = item.type.label, style = MaterialTheme.typography.labelMedium)
            Text(text = "약 ${item.distanceMeters}m", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null)
                Text(text = item.rating?.toString() ?: "-", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
