package com.gamsung2.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon

/** 홈 테마 카드에서 쓰는 데이터 모델 */
data class ThemeItem(
    val key: String,
    val title: String,
    val icon: ImageVector
)

/** 2열 컴팩트 그리드 (카드 높이 64dp) */
@Composable
fun ThemeGridCompact(
    items: List<ThemeItem>,
    onClick: (ThemeItem) -> Unit
) {
    val cardShape = RoundedCornerShape(12.dp)
    val gap = 8.dp
    val cardHeight = 64.dp
    val iconSize = 20.dp

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
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
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(item.icon, contentDescription = null, modifier = Modifier.size(iconSize))
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
