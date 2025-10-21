@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.place

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamsung2.domain.plan.Bucket
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.ui.components.NetworkImage
import com.gamsung2.ui.story.StoryViewModel

@Composable
fun PlaceDetailRoute(
    placeId: String,
    title: String,
    companion: String,
    lat: Double?,
    lon: Double?,
    onBack: () -> Unit,
    onNavigateToMap: (lat: Double?, lon: Double?, label: String?) -> Unit,
    // 권장: 명시 콜백
    onFindNearbyLodging: ((lat: Double?, lon: Double?) -> Unit)? = null,
    onFindNearbyFood: ((lat: Double?, lon: Double?) -> Unit)? = null,
    // 구버전 호환: 단일 콜백
    onFindNearby: (category: String, lat: Double, lon: Double, aroundTitle: String) -> Unit = { _,_,_,_ -> },

    // 🔵 추가: 선택 결과에 따라 이동 (기본은 no-op)
    onNavigateToMyTrip: (placeId: String, title: String) -> Unit = { _, _ -> },
    onNavigateToBucket: (placeId: String, title: String) -> Unit = { _, _ -> },

    // 필요 시 강제로 숨기고 싶을 때 사용 (기본은 보임)
    showNearbyButtons: Boolean = true,

    // 🔵 호출부 시그니처와의 호환을 위한 선택 파라미터 (현재 화면 내부 사용 X)
    initialBucket: Bucket? = null,
) {
    var savedScroll by rememberSaveable(placeId) { mutableStateOf(0) }
    val scrollState = rememberScrollState(initial = savedScroll)
    LaunchedEffect(scrollState.value) { savedScroll = scrollState.value }

    val hasCoord = lat != null && lon != null
    val safeTitle = title.ifBlank { "장소 상세" }
    val coordText = if (hasCoord) "좌표: ${"%.5f".format(lat)} , ${"%.5f".format(lon)}" else null

    val headerImageUrl: String? = null

    val activity = LocalContext.current as ComponentActivity
    val storyVm: StoryViewModel = viewModel(activity)

    // 표시용 뱃지: 버킷에 이미 있으면 체크 아이콘
    val alreadyInBucket by remember(placeId, storyVm.bucket) {
        derivedStateOf { storyVm.bucket.value.items.any { it.id == placeId } }
    }
    var picked by remember(placeId) { mutableStateOf(false) }
    LaunchedEffect(alreadyInBucket) { picked = alreadyInBucket }

    // 🔵 선택 시트 열림 상태
    var showCartPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(safeTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    // ✅ 토글 제거 → 선택 바텀시트 호출
                    AssistChip(
                        onClick = { showCartPicker = true },
                        label = { Text("여행 장바구니") },
                        leadingIcon = {
                            if (picked) Icon(Icons.Filled.CheckCircle, null)
                            else Icon(Icons.Filled.BookmarkAdd, null)
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NetworkImage(url = headerImageUrl, ratio = 4f / 3f, cornerRadius = 16f, modifier = Modifier.fillMaxWidth())

            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = safeTitle, style = MaterialTheme.typography.titleLarge)
                    if (companion.isNotBlank()) {
                        Text(text = "방문 유형: $companion", style = MaterialTheme.typography.labelLarge)
                    }
                    Text(
                        "장소 ID: $placeId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    coordText?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }

            // ▶ 버튼은 좌표가 있고(showNearbyButtons=true)일 때만 표시
            if (hasCoord && showNearbyButtons) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            onFindNearbyLodging?.invoke(lat, lon)
                                ?: onFindNearby("lodging", lat!!, lon!!, safeTitle)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("근처 숙소 찾기") }

                    OutlinedButton(
                        onClick = {
                            onFindNearbyFood?.invoke(lat, lon)
                                ?: onFindNearby("restaurant", lat!!, lon!!, safeTitle)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("근처 식당 찾기") }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("소개", style = MaterialTheme.typography.titleMedium)
                Text(
                    "이곳은 추천 명소입니다. 실제 서비스에서는 API의 상세 설명/편의시설/리뷰 요약 등을 표시합니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { onNavigateToMap(lat, lon, safeTitle) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("지도/길찾기") }
        }
    }

    // 🔵 바텀시트: 1회 여행 / 버킷리스트 선택
    if (showCartPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCartPicker = false },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            ListItem(
                headlineContent = { Text("1회 여행에 담기") },
                supportingContent = { Text("이번 여행 계획(나만의 여행)으로 이동") },
                leadingContent = { Icon(Icons.Filled.FlightTakeoff, null) },
                modifier = Modifier
                    .clickable {
                        showCartPicker = false
                        picked = false // 버킷 뱃지 해제(선택만 하고 버킷 추가는 아님)
                        onNavigateToMyTrip(placeId, safeTitle)
                    }
                    .padding(horizontal = 8.dp)
            )
            Divider()
            ListItem(
                headlineContent = { Text("버킷리스트에 담기") },
                supportingContent = { Text("나중에 가고 싶은 곳 리스트로 이동") },
                leadingContent = { Icon(Icons.Filled.BookmarkAdd, null) },
                modifier = Modifier
                    .clickable {
                        showCartPicker = false
                        picked = true
                        onNavigateToBucket(placeId, safeTitle)
                    }
                    .padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
