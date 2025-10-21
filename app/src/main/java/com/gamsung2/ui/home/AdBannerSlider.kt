// app/src/main/java/com/gamsung2/ui/home/AdBannerSlider.kt
package com.gamsung2.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdBannerSlider(
    modifier: Modifier = Modifier,
    ads: List<FestivalAd> = sampleAds(),  // 서버 연동 전 테스트용
    autoSlideMillis: Long = 3000L,
    onClick: (FestivalAd) -> Unit = {}
) {
    if (ads.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { ads.size })

    // ⏱ 자동 슬라이드
    LaunchedEffect(pagerState.currentPage, ads.size) {
        delay(autoSlideMillis)
        val next = (pagerState.currentPage + 1) % ads.size
        pagerState.animateScrollToPage(next)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp), // ✅ 110 -> 220 (2배)
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
                    .clickable { onClick(ad) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 썸네일 자리(실서비스: Coil/Glide AsyncImage 대체)
                Box(
                    modifier = Modifier
                        .size(160.dp) // ✅ 80 -> 160 (2배)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ad.city, style = MaterialTheme.typography.titleSmall)
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.titleLarge, // 조금 키움
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${ad.date} · ${ad.place}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = ad.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 샘플 데이터 (테스트용)
// 실제 연동 시에는 서버/DB 결과를 ads 파라미터로 주입하세요.
private fun sampleAds() = listOf(
    FestivalAd("ulsan-whale", "울산", "울산 고래축제", "10.10 ~ 10.13", "장생포 일대", "퍼레이드 · 불꽃쇼 · 가족체험"),
    FestivalAd("namhae-beer", "남해", "독일마을 맥주축제", "10.24 ~ 10.27", "독일마을", "라이브밴드와 함께하는 가을밤"),
    FestivalAd("jeongseon-market", "정선", "정선 5일장 특별전", "매월 2/7/12/17/22/27일", "정선아리랑시장", "지역 먹거리 · 공연 · 야시장")
)
