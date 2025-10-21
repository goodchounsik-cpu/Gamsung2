@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * 상단 SOS/교통/그룹 바
 * - DropdownMenu를 각 버튼과 같은 Box에 두어 앵커 위치 정확히 고정.
 */
@Composable
fun SosTopBar(
    title: String = "Gamsung2",
    subtitle: String? = null,
    isLoggedIn: Boolean = false,
    onGroupClick: () -> Unit = {},
    onProfile: () -> Unit = {},
    onLogin: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val ctx = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: 사용자명/부제
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 가운데 3분할 바 (너비 제한)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                        .widthIn(min = 180.dp, max = 260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SegmentedBar(
                        onGroup = onGroupClick,
                        onKtx = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.letskorail.com/")))
                        },
                        onBus = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://m.bustago.or.kr/")))
                        },
                        onRent = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=렌터카")))
                        },
                        onCall112 = {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                        },
                        onCall119 = {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
                        },
                        onFindToilet = {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=화장실")))
                        },
                        onShareMyLoc = {
                            val url = "https://maps.google.com/?q=37.5665,126.9780"
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "내 위치: $url")
                            }
                            ctx.startActivity(Intent.createChooser(share, "위치 공유"))
                        }
                    )
                }
            }
        },
        actions = {
            // 오른쪽 메뉴
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "메뉴")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    offset = DpOffset(0.dp, 8.dp)
                ) {
                    if (isLoggedIn) {
                        DropdownMenuItem(
                            text = { Text("내 정보") },
                            onClick = { menuOpen = false; onProfile() }
                        )
                        DropdownMenuItem(
                            text = { Text("로그아웃", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuOpen = false; onLogout() }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("로그인") },
                            onClick = { menuOpen = false; onLogin() }
                        )
                    }
                }
            }
        }
    )
}

/**
 * 가운데 3분할 바.
 * 교통수단/ SOS는 각 버튼을 Box로 감싸고 내부에 DropdownMenu를 둬서
 * 버튼 바로 아래에 메뉴가 정확히 붙도록 처리한다.
 */
@Composable
private fun SegmentedBar(
    onGroup: () -> Unit,
    // 교통수단 메뉴 액션
    onKtx: () -> Unit,
    onBus: () -> Unit,
    onRent: () -> Unit,
    // SOS 메뉴 액션
    onCall112: () -> Unit,
    onCall119: () -> Unit,
    onFindToilet: () -> Unit,
    onShareMyLoc: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 그룹 (드롭다운 없음)
        Segment(
            label = "그룹",
            container = Color(0xFF1976D2),
            content = Color.White,
            modifier = Modifier.weight(1f),
            onClick = onGroup
        )

        // 교통수단 (버튼 + 그 자리에서 드롭다운)
        var transportOpen by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            Segment(
                label = "교통수단",
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth(),
                onClick = { transportOpen = true }
            )
            DropdownMenu(
                expanded = transportOpen,
                onDismissRequest = { transportOpen = false },
                offset = DpOffset(0.dp, 6.dp)
            ) {
                CenterItem("KTX") { transportOpen = false; onKtx() }
                CenterItem("버스") { transportOpen = false; onBus() }
                CenterItem("렌트") { transportOpen = false; onRent() }
            }
        }

        // SOS (버튼 + 그 자리에서 드롭다운)
        var sosOpen by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            Segment(
                label = "SOS",
                container = Color(0xFFE53935),
                content = Color.White,
                modifier = Modifier.fillMaxWidth(),
                onClick = { sosOpen = true }
            )
            DropdownMenu(
                expanded = sosOpen,
                onDismissRequest = { sosOpen = false },
                offset = DpOffset(0.dp, 6.dp)
            ) {
                CenterItem("112 (경찰) 전화걸기") { sosOpen = false; onCall112() }
                CenterItem("119 (화재/구급) 전화걸기") { sosOpen = false; onCall119() }
                CenterItem("화장실 찾기") { sosOpen = false; onFindToilet() }
                CenterItem("내 위치 보내기") { sosOpen = false; onShareMyLoc() }
            }
        }
    }
}

@Composable
private fun Segment(
    label: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = content, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CenterItem(label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(label)
            }
        },
        onClick = onClick
    )
}
