// app/src/main/java/com/gamsung2/ui/components/NetworkImage.kt
package com.gamsung2.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
// Accompanist placeholder(material) — foundation.placeholder와 충돌 피하려고 별칭 사용
import com.google.accompanist.placeholder.material.placeholder as acPlaceholder

/**
 * 고정 비율 + 플레이스홀더 + 플리커(깜빡임) 최소화 이미지
 */
@Composable
fun NetworkImage(
    url: String?,
    modifier: Modifier = Modifier,
    ratio: Float = 1.6f,
    cornerRadius: Float = 16f,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val safeRatio = if (ratio > 0f) ratio else 1f
    val shape = RoundedCornerShape(cornerRadius.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(safeRatio),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (url.isNullOrBlank()) {
            PlaceholderBox()
            return@Surface
        }

        val context = LocalContext.current
        val request = remember(url) {
            ImageRequest.Builder(context)
                .data(url)
                .crossfade(false) // 플리커 최소화
                .allowHardware(true)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
        }

        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            contentScale = contentScale,
            loading = { PlaceholderBox() },
            error   = { PlaceholderBox() },
            success = { SubcomposeAsyncImageContent() }
        )
    }
}

@Composable
private fun PlaceholderBox() {
    Box(
        Modifier
            .fillMaxSize()
            .acPlaceholder(
                visible = true
                // NOTE: 버전 이슈로 shimmer 하이라이트는 일단 제외.
                // 필요하면 나중에 `highlight = PlaceholderHighlight.shimmer()` 혹은 `highlight = shimmer()` 추가
            )
    )
}
