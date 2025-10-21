@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.model.PlaceCandidate
import com.gamsung2.planner.PlannerViewModel
import kotlinx.coroutines.delay

@Composable
fun UnifiedSearchScreen(
    initialQuery: String,
    onClose: () -> Unit,
    onAddToPlan: (PlanItem) -> Unit,
    vm: PlannerViewModel = hiltViewModel()
) {
    var q by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<PlaceCandidate>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val kb = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    fun runSearch() {
        if (q.isBlank()) return
        isSearching = true
        vm.searchAll(q, null, null) {
            results = it
            isSearching = false
        }
    }

    // 최초 진입 시 자동 포커스 + 키보드 열기
    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        kb?.show()
        if (q.isNotBlank()) runSearch()
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                        OutlinedTextField(
                            value = q,
                            onValueChange = { q = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = { Text("장소 / 작품명 / 카테고리") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, null) },
                            trailingIcon = {
                                if (q.isNotEmpty()) {
                                    IconButton(onClick = { q = "" }) {
                                        Icon(Icons.Filled.Clear, contentDescription = "지우기")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search,
                                capitalization = KeyboardCapitalization.None,
                                keyboardType = KeyboardType.Text,
                                autoCorrect = true
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    kb?.hide()
                                    runSearch()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        FilledTonalButton(
                            onClick = { kb?.hide(); runSearch() },
                            enabled = q.isNotBlank(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("검색") }
                    }
                }
            }
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
        ) {
            when {
                isSearching -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("검색 중…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                results.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("검색어를 입력해보세요", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "장소명 · 작품명 · 카테고리로 검색할 수 있어요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results, key = { it.place.id.toString() }) { pc ->
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(pc.place.name, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        pc.source.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(
                                            onClick = {
                                                val plan = PlanItem(
                                                    id = pc.place.id.toString(),
                                                    title = pc.place.name
                                                )
                                                onAddToPlan(plan)
                                                vm.add(pc)
                                            }
                                        ) { Text("담기") }
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
