package com.gamsung2.ui.place.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 평점 모델 (식당/숙소 공용)
sealed class RatingFilter(val label: String, val min: Float?) {
    data object All : RatingFilter("전체", null)
    data object Star3 : RatingFilter("★3.0+", 3.0f)
    data object Star4 : RatingFilter("★4.0+", 4.0f)
    data object Star45 : RatingFilter("★4.5+", 4.5f)

    companion object {
        val items = listOf(All, Star3, Star4, Star45)
    }
}

/** 단일 선택형 평점 칩 라인 (식당/숙소 모두에서 재사용) */
@Composable
fun RatingFilterRow(
    selected: RatingFilter,
    onChange: (RatingFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RatingFilter.items.forEach { item ->
            FilterChip(
                selected = (item::class == selected::class),
                onClick = { onChange(item) },
                label = { Text(item.label) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Star, contentDescription = null) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}
