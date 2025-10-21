package com.gamsung2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gamsung2.data.ads.LocalBizAd

@Composable
fun LocalBizAdCarousel(
    ads: List<LocalBizAd>,
    onClick: (LocalBizAd) -> Unit
) {
    if (ads.isEmpty()) return

    val listState = rememberLazyListState()
    val fling = rememberSnapFlingBehavior(listState)

    LazyRow(
        state = listState,
        flingBehavior = fling,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // 필요시 높이만 여기서 조절
    ) {
        items(ads, key = { it.id }) { ad ->
            Surface(
                tonalElevation = 0.dp,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, DividerDefaults.color.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillParentMaxWidth(0.92f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onClick(ad) }
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(ad.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)

                        // subtitle 안전 처리 (!! 제거)
                        ad.subtitle
                            ?.takeIf { it.isNotBlank() }
                            ?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1) }

                        Text(ad.bizName, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("이미지", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
