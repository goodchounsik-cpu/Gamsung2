// app/src/main/java/com/gamsung2/ui/place/components/PlaceShimmers.kt
package com.gamsung2.ui.place.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp          // ✅ 추가
import androidx.compose.ui.unit.dp
// foundation.placeholder 와 충돌 방지: 별칭 임포트
import com.google.accompanist.placeholder.material.placeholder as acPlaceholder

@Composable
fun PlaceCardShimmer(modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            // 이미지 자리 (16:9)
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .acPlaceholder(visible = true)
            )
            Spacer(Modifier.height(10.dp))
            // 타이틀 자리
            Box(
                Modifier
                    .fillMaxWidth(0.7f)
                    .height(18.dp)
                    .acPlaceholder(visible = true)
            )
            Spacer(Modifier.height(8.dp))
            // 부제목 자리
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .acPlaceholder(visible = true)
            )
            Spacer(Modifier.height(8.dp))
            // 하단 라인 2개
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .acPlaceholder(visible = true)
                )
                Box(
                    Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .acPlaceholder(visible = true)
                )
            }
        }
    }
}

@Composable
fun EndOfListSpacer(height: Dp = 24.dp) {
    Spacer(Modifier.height(height))
}
