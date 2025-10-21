// app/src/main/java/com/gamsung2/ui/home/FestivalTVCard.kt
package com.gamsung2.ui.home

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

// ★★★ Media3 import로 교체 ★★★
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import kotlinx.coroutines.delay

@Composable
fun FestivalTVCard(
    media: FestivalMedia,
    modifier: Modifier = Modifier,
    onClick: (FestivalMedia) -> Unit = {}
) {
    Column(modifier) {
        // 화면부 (16:9) + TV 프레임
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        ) {
            Box(Modifier.fillMaxSize()) {
                when {
                    !media.videoUrl.isNullOrBlank() ->
                        VideoPlayerTV(url = media.videoUrl!!, onClick = { onClick(media) })
                    media.imageUrls.isNotEmpty() ->
                        ImageSliderTV(urls = media.imageUrls, onClick = { onClick(media) })
                    else ->
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
                }

                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "📺 ${media.city} · ${media.title}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF444444))
            )
            Box(
                Modifier
                    .width(36.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF444444))
            )
        }
    }
}

/* -------------------- 내부: 동영상/이미지 구현 -------------------- */

@Composable
private fun VideoPlayerTV(url: String, onClick: () -> Unit) {
    val context = LocalContext.current

    // Media3 ExoPlayer 생성/해제
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f           // 무음 자동재생
            playWhenReady = true
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}

@Composable
private fun ImageSliderTV(urls: List<String>, onClick: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(urls) {
        while (true) {
            delay(3000)
            if (urls.isNotEmpty()) index = (index + 1) % urls.size
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = urls.getOrNull(index),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
