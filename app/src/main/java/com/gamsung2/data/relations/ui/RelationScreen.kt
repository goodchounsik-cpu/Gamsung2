@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.gamsung2.data.relations.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamsung2.model.RelationState
import kotlinx.coroutines.launch

@Composable
fun RelationScreen(
    meId: String = "010-0000-0000",
    vm: RelationViewModel = viewModel()
) {
    // 최초 1회 부팅
    LaunchedEffect(meId) { vm.boot(meId) }

    val rows by vm.rows.collectAsState()
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("회원 공유(상호 승인)") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("상대 회원(전화번호)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val phone = input.trim()
                        if (phone.isNotEmpty()) scope.launch { vm.sendRequest(phone) }
                        input = ""
                    },
                    enabled = input.trim().isNotEmpty()
                ) { Text("요청 보내기") }

                OutlinedButton(onClick = { scope.launch { vm.refresh() } }) {
                    Text("새로고침")
                }
            }

            Text("내 관계", style = MaterialTheme.typography.titleMedium)

            if (rows.isEmpty()) {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        Modifier.padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) { Text("관계가 없습니다. 상단에서 요청을 보내보세요.") }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rows, key = { it.otherId }) { row ->
                        Surface(
                            tonalElevation = 1.dp,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        row.otherId,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val stateLabel = when (row.stateForMe) {
                                        RelationState.PENDING_RECV -> "상대 요청 도착(내가 수락/거절)"
                                        RelationState.PENDING_SENT -> "내가 보낸 요청(대기)"
                                        RelationState.ACCEPTED     -> "공유 중"
                                        RelationState.REJECTED     -> "거절됨"
                                        else                       -> ""
                                    }
                                    Text(stateLabel, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.secondaryLabel?.let { label ->
                                        OutlinedButton(onClick = { /* TODO: secondaryClick 연결 */ }) {
                                            Text(label)
                                        }
                                    }
                                    row.primaryLabel?.let { label ->
                                        Button(onClick = { /* TODO: primaryClick 연결 */ }) {
                                            Text(label)
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
