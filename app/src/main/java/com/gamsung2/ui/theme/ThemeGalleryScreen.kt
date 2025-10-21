// app/src/main/java/com/gamsung2/ui/theme/ThemeGalleryScreen.kt
package com.gamsung2.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

/* ────────────────────────────────
 * 권장: 명시적 images를 넘기는 공용 API
 * AppNav에선 이 버전을 쓰는 걸 추천
 * ──────────────────────────────── */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ThemeGalleryScreen(
    title: String,
    images: List<String>,
    onBack: () -> Unit
) {
    val safeImages = if (images.isEmpty()) listOf("https://picsum.photos/seed/empty_1/1200/800") else images
    val pager = rememberPagerState(initialPage = 0, pageCount = { safeImages.size })

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        TopAppBar(
            title = { Text(text = title.ifBlank { "테마 갤러리" }, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // TopAppBar 높이
        ) {
            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                AsyncImage(
                    model = safeImages[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(safeImages.size) { idx ->
                    val active = idx == pager.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (active) 10.dp else 6.dp)
                            .background(
                                color = if (active) Color.White else Color(0x66FFFFFF),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

/* ────────────────────────────────
 * 편의 ①: count만 넘기는 경우
 * 예) ThemeGalleryScreen(title = "...", count = 3, onBack = { ... })
 * ──────────────────────────────── */
@Composable
fun ThemeGalleryScreen(
    title: String,
    count: Int,
    onBack: () -> Unit
) {
    val images = remember(count) {
        val c = count.coerceAtLeast(1)
        (1..c).map { i -> "https://picsum.photos/seed/default_$i/1200/800" }
    }
    ThemeGalleryScreen(title = title, images = images, onBack = onBack)
}

/* ────────────────────────────────
 * 편의 ②: 구버전 호환(네비게이션 컨트롤러를 직접 넘기던 호출부용)
 * 예) ThemeGalleryScreen(navController, themeKey, selectedIds, themeTitle)
 * ──────────────────────────────── */
@Composable
fun ThemeGalleryScreen(
    navController: NavHostController,
    themeKey: String,
    selectedIds: List<Int>,
    themeTitle: String
) {
    val images = remember(themeKey, selectedIds) {
        val seeds = if (selectedIds.isEmpty()) listOf(1, 2, 3, 4) else selectedIds
        seeds.map { id -> "https://picsum.photos/seed/${themeKey}_${id}/1200/800" }
    }
    ThemeGalleryScreen(
        title = themeTitle,
        images = images,
        onBack = { navController.popBackStack() }
    )
}
