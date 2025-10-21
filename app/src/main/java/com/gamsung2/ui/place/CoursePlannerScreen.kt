// app/src/main/java/com/gamsung2/ui/place/CoursePlannerScreen.kt
package com.gamsung2.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 화면 전용 파일.
 * ⚠️ ViewModel/데이터 모델은 CoursePlannerViewModel.kt 한 곳에만 존재해야 함.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePlannerScreen(
    vm: CoursePlannerViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("코스짜기") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } },
                actions = { TextButton(onClick = { vm.savePlan() }, enabled = !ui.loading) { Text("저장") } }
            )
        },
        bottomBar = {
            PlannerBottomBar(
                total = ui.metrics.totalTimeMin,
                move  = ui.metrics.moveTimeMin,
                budget = ui.metrics.budget,
                onSave = { vm.savePlan() }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            PlannerControls(
                filters      = ui.filters,
                onSeed       = vm::updateSeed,
                onDays       = vm::updateDays,
                onRegion     = vm::updateRegion,
                onRecommend  = vm::fetchCandidates,
                onAuto       = vm::autoAssemble,
                loading      = ui.loading
            )
            Spacer(Modifier.height(8.dp))
            PlannerPager(
                ui = ui,
                onAddToDay = vm::addToDay,
                onRemove   = vm::removeItem
            )
        }
    }
}

/* ---------- Controls ---------- */
@Composable
private fun PlannerControls(
    filters: PlannerFilters,
    onSeed: (String) -> Unit,
    onDays: (Int) -> Unit,
    onRegion: (String) -> Unit,
    onRecommend: () -> Unit,
    onAuto: () -> Unit,
    loading: Boolean
) {
    Surface(tonalElevation = 1.dp) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filters.seed,
                onValueChange = onSeed,
                label = { Text("키워드") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            DaysPicker(filters.days, onDays)
            RegionChip(filters.region, onRegion)

            FilledTonalButton(onClick = onRecommend, enabled = !loading) { Text("추천") }
            Button(onClick = onAuto, enabled = !loading) { Text("자동") }
        }
    }
}

@Composable
private fun DaysPicker(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("일수")
        OutlinedButton(onClick = { onChange(value - 1) }, enabled = value > 1) { Text("-") }
        Text("$value")
        OutlinedButton(onClick = { onChange(value + 1) }, enabled = value < 7) { Text("+") }
    }
}

@Composable
private fun RegionChip(region: String, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { open = true },
            label = { Text(region) },
            leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) }
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf("서울", "부산", "대구", "인천", "광주", "대전", "제주").forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { open = false; onChange(it) })
            }
        }
    }
}

/* ---------- Pager (탭 3개) ---------- */
@Composable
private fun PlannerPager(
    ui: CoursePlannerUi,
    onAddToDay: (Int, PlanItemLite) -> Unit,
    onRemove: (Int, String) -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("탐색", "빌더", "지도")

    TabRow(selectedTabIndex = tab) {
        tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
    }

    when (tab) {
        0 -> DiscoverTab(ui, onAddToDay)
        1 -> BuilderTab(ui, onRemove)
        else -> MapTab()
    }
}

/* ---- Tab 1: 후보 탐색 ---- */
@Composable
private fun DiscoverTab(
    ui: CoursePlannerUi,
    onAddToDay: (Int, PlanItemLite) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        if (ui.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(ui.candidates, key = { it.id }) { p ->
                ElevatedCard {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(p.title, style = MaterialTheme.typography.titleSmall)
                            Text(p.category, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (0 until ui.filters.days).forEach { day ->
                                OutlinedButton(onClick = { onAddToDay(day, p) }) { Text("${day + 1}일차") }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---- Tab 2: 빌더 ---- */
@Composable
private fun BuilderTab(
    ui: CoursePlannerUi,
    onRemove: (Int, String) -> Unit
) {
    Row(
        Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ui.itinerary.days.forEachIndexed { dayIdx, day ->
            ElevatedCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Column(Modifier.padding(10.dp)) {
                    Text("${dayIdx + 1}일차", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (day.items.isEmpty()) {
                        Box(
                            Modifier.fillMaxWidth().weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text("여기에 담아보세요") }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(day.items, key = { it.id }) { item ->
                                Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.title)
                                            Text(item.category, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { onRemove(dayIdx, item.id) }) {
                                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---- Tab 3: 지도(자리만) ---- */
@Composable
private fun MapTab() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("지도가 여기에 들어갑니다.")
    }
}

/* ---------- Bottom Bar ---------- */
@Composable
private fun PlannerBottomBar(
    total: Int,
    move: Int,
    budget: Int,
    onSave: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("총 ${total}분, 이동 ${move}분")
                if (budget > 0) Text("예산 ${budget}원")
            }
            Button(onClick = onSave) { Text("일정 저장") }
        }
    }
}
