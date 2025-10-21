@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamsung2.domain.plan.Bucket
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.ui.story.StoryViewModel

/**
 * 홈 상단 "나만의 허브" 카드
 * - 탭: [나만의 여행스토리] / [나만의 버킷여행]
 * - 접기/펼치기 토글
 * - 액션: 통합 검색, 코스짜기
 *  -> MyHubTabs.kt 없이 이 컴포넌트 하나로 처리합니다.
 */
@Composable
fun MyTravelCard(
    vm: StoryViewModel,
    onOpenSearch: (Bucket?) -> Unit = {},   // 미전달 시 no-op
    onOpenPlanner: () -> Unit = {},
    onOpenItem: (PlanItem) -> Unit = {}
) {
    val story = vm.story.collectAsState().value
    val bucket = vm.bucket.collectAsState().value

    // 0 = 여행스토리, 1 = 버킷여행
    var selected by rememberSaveable { mutableStateOf(0) }
    var expanded by rememberSaveable { mutableStateOf(true) }  // 기본 펼침

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {

            /* 헤더: 탭 + 코스짜기 + 접기버튼 */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegTab(
                        text = "나만의 여행",
                        selected = selected == 0,
                        onClick = {
                            val wasOtherTab = selected != 0
                            selected = 0
                            expanded = if (wasOtherTab) true else !expanded
                        }
                    )
                    SegTab(
                        text = "나만의 버킷",
                        selected = selected == 1,
                        onClick = {
                            val wasOtherTab = selected != 1
                            selected = 1
                            expanded = if (wasOtherTab) true else !expanded
                        }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onOpenPlanner) {
                        Text("코스짜기")
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }

            /* 본문 */
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(10.dp))

                    // 상단 액션 라인
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onOpenSearch(null) }) {
                            Text("통합 검색")
                            Icon(Icons.Filled.ChevronRight, contentDescription = null)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (selected == 0) {
                        /* 여행스토리: 코스/식당/숙소 */
                        SectionBlock(title = "여행코스", titleIcon = { Icon(Icons.Filled.Map, null) }) {
                            SubRow(
                                label = "여행코스",
                                items = story.courses ?: emptyList(),
                                icon = { Icon(Icons.Filled.Map, null) },
                                onOpenItem = onOpenItem,
                                onAdd = { onOpenSearch(Bucket.COURSE) }
                            )
                            Spacer(Modifier.height(6.dp))
                            SubRow(
                                label = "식당",
                                items = story.foods ?: emptyList(),
                                icon = { Icon(Icons.Filled.Restaurant, null) },
                                onOpenItem = onOpenItem,
                                onAdd = { onOpenSearch(Bucket.FOOD) }
                            )
                            Spacer(Modifier.height(6.dp))
                            SubRow(
                                label = "숙소",
                                items = story.lodgings ?: emptyList(),
                                icon = { Icon(Icons.Filled.Hotel, null) },
                                onOpenItem = onOpenItem,
                                onAdd = { onOpenSearch(Bucket.LODGING) }
                            )
                        }
                    } else {
                        /* 버킷여행: 담긴 항목 칩 + 추가 */
                        SectionBlock(title = "버킷 항목", titleIcon = { Icon(Icons.Filled.Map, null) }) {
                            val items = bucket.items ?: emptyList()
                            if (items.isEmpty()) {
                                Text(
                                    "담긴 항목이 없습니다.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(items, key = { it.id }) { it ->
                                        AssistChip(
                                            onClick = { onOpenItem(it) },
                                            label = { Text(it.title, maxLines = 1) }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onOpenSearch(null) }) {
                                    Text("추가")
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- UI 파츠 ---------- */

@Composable
private fun SegTab(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = if (selected) ButtonDefaults.filledTonalButtonColors()
    else ButtonDefaults.outlinedButtonColors()

    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        modifier = Modifier.height(34.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SectionBlock(
    title: String,
    titleIcon: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            titleIcon()
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun SubRow(
    label: String,
    items: List<PlanItem>,
    icon: @Composable () -> Unit,
    onOpenItem: (PlanItem) -> Unit,
    onAdd: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                icon()
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            TextButton(onClick = onAdd, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text("추가", style = MaterialTheme.typography.labelLarge)
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        Spacer(Modifier.height(4.dp))

        if (items.isEmpty()) {
            Text(
                "담긴 항목이 없습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { it ->
                    AssistChip(
                        onClick = { onOpenItem(it) },
                        label = { Text(it.title, maxLines = 1) }
                    )
                }
            }
        }
    }
}
