@file:OptIn(ExperimentalMaterial3Api::class)

package com.gamsung2.ui.translate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamsung2.MapViewModel
import com.gamsung2.data.local.FavoritePlaceEntity
import com.gamsung2.ui.FavTopBarActions

enum class SortType { LATEST, NAME }
enum class FilterType { ALL, HAS_NOTE }

@Composable
fun FavListScreen(
    vm: MapViewModel,
    onBack: () -> Unit,
    onEdit: (FavoritePlaceEntity) -> Unit,
    onSnack: (String) -> Unit
) {
    val places by vm.places.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(SortType.LATEST) }
    var filter by remember { mutableStateOf(FilterType.ALL) }
    var menuOpen by remember { mutableStateOf(false) }

    val view by remember(places, query, sort, filter) {
        derivedStateOf {
            places
                .asSequence()
                .filter {
                    val okQuery = query.isBlank() ||
                            it.name.contains(query, ignoreCase = true) ||
                            (it.note?.contains(query, ignoreCase = true) == true)
                    val okFilter = when (filter) {
                        FilterType.ALL -> true
                        FilterType.HAS_NOTE -> !it.note.isNullOrBlank()
                    }
                    okQuery && okFilter
                }
                .let { seq ->
                    when (sort) {
                        SortType.LATEST -> seq.sortedByDescending { it.id }
                        SortType.NAME -> seq.sortedBy { it.name.lowercase() }
                    }
                }
                .toList()
        }
    }

    val count = view.size
    val maxCount = 100

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                title = { Text("나만의 버킷리스트") },
                actions = {
                    Text("${count}/${maxCount}")
                    Spacer(Modifier.width(6.dp))
                    FavTopBarActions(vm, onSnack)
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("정렬: 최신순") },
                            onClick = { sort = SortType.LATEST; menuOpen = false }
                        )
                        DropdownMenuItem(
                            text = { Text("정렬: 이름순") },
                            onClick = { sort = SortType.NAME; menuOpen = false }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("필터: 전체") },
                            onClick = { filter = FilterType.ALL; menuOpen = false }
                        )
                        DropdownMenuItem(
                            text = { Text("필터: 메모 있는 항목") },
                            onClick = { filter = FilterType.HAS_NOTE; menuOpen = false }
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                label = { Text("검색 (이름/메모)") },
                singleLine = true
            )

            // 2열 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(view, key = { it.id }) { p ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 92.dp)
                            .clickable { onEdit(p) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(p.name, style = MaterialTheme.typography.titleSmall)
                            if (!p.note.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    p.note!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            // 미션완료 뱃지 골격(예: note에 [done] 포함 시)
                            val missionDone = p.note?.contains("[done]", ignoreCase = true) == true
                            if (missionDone) {
                                Spacer(Modifier.height(6.dp))
                                AssistChip(
                                    onClick = { /* no-op */ },
                                    label = { Text("미션완료") }
                                )
                            } else {
                                Spacer(Modifier.height(6.dp))
                                AssistChip(
                                    onClick = { /* 사진 인증 유도: 향후 카메라/갤러리 연동 */ },
                                    label = { Text("사진 인증하고 미션완료!") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
