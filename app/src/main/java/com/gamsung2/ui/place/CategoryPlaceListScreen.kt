// app/src/main/java/com/gamsung2/ui/place/CategoryPlaceListScreen.kt
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.gamsung2.ui.place

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gamsung2.model.Place
import com.gamsung2.nav.Routes
import com.gamsung2.ui.place.components.PlaceGridPaged
import kotlin.math.roundToInt

/* ─ UI 토큰 (컴팩트) ─ */
private val SECTION_PAD    = 6.dp
private val LABEL_GAP      = 3.dp
private val CHIP_H_SPACING = 3.dp
private val SEG_HEIGHT     = 28.dp
private val CARD_HEIGHT    = 208.dp

@Composable
fun CategoryPlaceListScreen(
    navController: NavController,
    category: String,           // "lodging" | "restaurant"
    title: String,
    themeTitle: String,
    lat: Double?,
    lng: Double?,
    radiusKm: Double,
    showWide: Boolean           // ✅ 추가: 넓게 보기(필터 숨김)
) {
    val isLodging = category == "lodging"
    val isRestaurant = category == "restaurant"

    val vm: CategoryPlaceListViewModel = hiltViewModel()

    // 최초 1회 초기화
    LaunchedEffect(category, themeTitle, lat, lng, radiusKm) {
        vm.ensureInitialized(
            category = category,
            themeTitle = themeTitle,
            initLat = lat,
            initLng = lng,
            initRadiusKm = radiusKm
        )
    }

    val ui = vm.uiState.collectAsState().value

    val radiusOptions = listOf(0.5, 1.0, 3.0, 5.0, 10.0)
    val ratingSteps   = listOf(0.0, 3.0, 4.0, 4.5)  // 0.0=전체

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // 같은 파라미터로 wide 토글 라우팅
                            navController.navigate(
                                Routes.categoryPlaceListRoute(
                                    category = category,
                                    title = title,
                                    themeTitle = themeTitle,
                                    lat = lat,
                                    lng = lng,
                                    radiusKm = ui.radiusKm,
                                    wide = !showWide
                                )
                            )
                        }
                    ) { Text(if (showWide) "필터 보기" else "넓게 보기") }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            /* ─ 필터 박스 (컴팩트) ─ */
            if (!showWide) {
                Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SECTION_PAD, vertical = SECTION_PAD),
                        verticalArrangement = Arrangement.spacedBy(SECTION_PAD)
                    ) {
                        // 거리
                        Text("거리", style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow {
                            radiusOptions.forEachIndexed { index, km ->
                                SegmentedButton(
                                    modifier = Modifier.height(SEG_HEIGHT),
                                    selected = ui.radiusKm == km,
                                    onClick = { vm.setRadius(km) },
                                    shape = SegmentedButtonDefaults.itemShape(index, radiusOptions.size),
                                    label = {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (km < 1) "${(km * 1000).roundToInt()}m" else "${km.roundToInt()}km",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp)

                        // 숙소 타입 (2열 그리드)
                        if (isLodging) {
                            Column(verticalArrangement = Arrangement.spacedBy(LABEL_GAP)) {
                                Text("숙소 타입", style = MaterialTheme.typography.labelLarge)
                                LodgingType.values().toList().chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(CHIP_H_SPACING)
                                    ) {
                                        rowItems.forEach { t ->
                                            FilterChip(
                                                modifier = Modifier
                                                    .height(SEG_HEIGHT)
                                                    .weight(1f),
                                                selected = ui.lodgingTypes.contains(t),
                                                onClick = { vm.toggleLodgingType(t) },
                                                label = {
                                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text(t.label, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                        }

                        // 음식 (2열 그리드)
                        if (isRestaurant) {
                            Column(verticalArrangement = Arrangement.spacedBy(LABEL_GAP)) {
                                Text("음식", style = MaterialTheme.typography.labelLarge)
                                CuisineType.values().toList().chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(CHIP_H_SPACING)
                                    ) {
                                        rowItems.forEach { c ->
                                            FilterChip(
                                                modifier = Modifier
                                                    .height(SEG_HEIGHT)
                                                    .weight(1f),
                                                selected = ui.cuisines.contains(c),
                                                onClick = { vm.toggleCuisine(c) },
                                                label = {
                                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        Text(c.label, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                        }

                        // 평점
                        Text("평점", style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow {
                            ratingSteps.forEachIndexed { index, step ->
                                SegmentedButton(
                                    modifier = Modifier.height(SEG_HEIGHT),
                                    selected = ui.minRating == step,
                                    onClick = { vm.updateMinRating(step) },
                                    shape = SegmentedButtonDefaults.itemShape(index, ratingSteps.size),
                                    label = {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (step == 0.0) "전체" else "★${step}+",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        // 선택 요약
                        AssistChip(
                            modifier = Modifier.height(SEG_HEIGHT),
                            onClick = { /* no-op */ },
                            label = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    val around = if (lat != null && lng != null) "주변 ${ui.radiusKm}km" else themeTitle
                                    Text("${if (isLodging) "숙소" else "식당"} · $around", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            leadingIcon = {
                                Icon(if (isLodging) Icons.Filled.Bed else Icons.Filled.Restaurant, contentDescription = null)
                            }
                        )
                    }
                }
                HorizontalDivider()
            }

            /* 컨텐츠 */
            when {
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                ui.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("불러오는 중 오류가 발생했어요.\n${ui.error}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { vm.refresh() }) { Text("다시 시도") }
                    }
                }
                ui.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("조건에 맞는 결과가 없어요.", style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    PlaceGridPaged(
                        items = ui.items,
                        isLoading = vm.isLoadingMore,
                        onLoadMore = { vm.loadNext() },
                        itemContent = { p ->
                            PlaceGridCard(
                                item = p,
                                isLodging = isLodging,
                                isRestaurant = isRestaurant,
                                onClick = {
                                    navController.navigate(
                                        Routes.placeDetailRoute(
                                            placeId   = p.id,
                                            companion = p.badge.orEmpty(),
                                            title     = p.name
                                        )
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

/* ─ 카드(화면 전용 프레젠테이션) ─ */
@Composable
private fun PlaceGridCard(
    item: Place,
    isLodging: Boolean,
    isRestaurant: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(CARD_HEIGHT),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(if (isLodging) Icons.Filled.Bed else Icons.Filled.Restaurant, contentDescription = null)

            Text(
                text = item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.subtitle.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (!item.badge.isNullOrBlank()) {
                    AssistChip(onClick = {}, label = { Text(item.badge!!) })
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dist = item.distanceKm
                Text(
                    text = dist?.let { if (it < 1) "약 ${(it * 1000).toInt()}m" else "약 ${"%.1f".format(it)}km" } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                val starText = item.rating?.let { "★ ${"%.1f".format(it)}" } ?: "★ —"
                AssistChip(onClick = {}, label = { Text(starText) })
            }
        }
    }
}
