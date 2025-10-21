// app/src/main/java/com/gamsung2/ui/home/AdSliderBox.kt
package com.gamsung2.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdSliderBox(
    modifier: Modifier = Modifier,
    ads: List<FestivalAd> = sampleAds(),
    autoSlideMillis: Long = 3000L,
    onClick: (FestivalAd) -> Unit = {}
) {
    if (ads.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { ads.size })

    // ⏱️ 자동 슬라이드
    LaunchedEffect(pagerState.currentPage, ads.size) {
        delay(autoSlideMillis)
        val next = (pagerState.currentPage + 1) % ads.size
        pagerState.animateScrollToPage(next)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val ad = ads[page]
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 썸네일 자리 – 실제로는 AsyncImage 등으로 교체
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ad.city, style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${ad.date} · ${ad.place}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = ad.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- 데이터 모델 & 샘플 ---

private fun sampleAds() = listOf(
    FestivalAd(
        id = "ulsan-whale",
        city = "울산",
        title = "울산 고래축제",
        date = "10.10 ~ 10.13",
        place = "장생포 일대",
        tagline = "퍼레이드 · 불꽃쇼 · 가족체험"
    ),
    FestivalAd(
        id = "namhae-beer",
        city = "남해",
        title = "독일마을 맥주축제",
        date = "10.24 ~ 10.27",
        place = "독일마을",
        tagline = "라이브밴드와 함께하는 가을밤"
    ),
    FestivalAd(
        id = "jeongseon-market",
        city = "정선",
        title = "정선 5일장 특별전",
        date = "매월 2/7/12/17/22/27일",
        place = "정선아리랑시장",
        tagline = "지역 먹거리 · 공연 · 야시장"
    )
)
