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
import com.gamsung2.model.RelationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/* ───────── 더미 엔티티(이 파일 전용) ───────── */
private data class MockRelation(
    val meId: String,
    val otherId: String,
    val state: RelationState,
    val createdAt: Long = System.currentTimeMillis()
)

/* ───────── 인메모리 API(테스트용) ───────── */
private class MockRelationApi {
    private val map = LinkedHashMap<Set<String>, MockRelation>() // key = setOf(a,b)

    fun snapshot(): List<MockRelation> = map.values.toList()

    fun sendRequest(from: String, to: String) {
        val k = setOf(from, to)
        map[k] = MockRelation(meId = from, otherId = to, state = RelationState.PENDING_SENT)
    }

    fun cancelRequest(from: String, to: String) {
        map.remove(setOf(from, to))
    }

    fun accept(a: String, b: String) {
        val k = setOf(a, b)
        map[k]?.let { map[k] = it.copy(state = RelationState.ACCEPTED) }
    }

    fun reject(a: String, b: String) {
        val k = setOf(a, b)
        map[k]?.let { map[k] = it.copy(state = RelationState.REJECTED) }
    }
}

/* ───────── UI용 더미 리포지토리(이름 충돌 방지) ───────── */
class FakeRelationRepository(
    private val meId: String
) {
    // ✅ private 타입을 외부에 노출하지 않도록 내부에서 생성
    private val api = MockRelationApi()

    private val _myRelations = MutableStateFlow(listOf<RowView>())
    val myRelations: StateFlow<List<RowView>> = _myRelations

    data class RowView(
        val otherId: String,
        val stateForMe: RelationState,
        val primaryAction: (suspend () -> Unit)? = null,
        val primaryLabel: String? = null,
        val secondaryAction: (suspend () -> Unit)? = null,
        val secondaryLabel: String? = null
    )

    init { refresh() }

    fun refresh() {
        val result = api.snapshot().mapNotNull { r ->
            val a = r.meId; val b = r.otherId

            val (other, stateForMe) = when {
                r.state == RelationState.PENDING_SENT && a == meId -> b to RelationState.PENDING_SENT
                r.state == RelationState.PENDING_SENT && b == meId -> a to RelationState.PENDING_RECV
                a == meId -> b to r.state
                b == meId -> a to r.state
                else -> return@mapNotNull null
            }

            var pAct: (suspend () -> Unit)? = null
            var pLabel: String? = null
            var sAct: (suspend () -> Unit)? = null
            var sLabel: String? = null

            when (stateForMe) {
                RelationState.PENDING_SENT -> {
                    pAct = { api.cancelRequest(meId, other); refresh() }
                    pLabel = "요청취소"
                }
                RelationState.PENDING_RECV -> {
                    pAct = { api.accept(meId, other); refresh() }
                    pLabel = "수락"
                    sAct = { api.reject(meId, other); refresh() }
                    sLabel = "거절"
                }
                RelationState.ACCEPTED -> {
                    sAct = { api.reject(meId, other); refresh() }
                    sLabel = "해제"
                }
                RelationState.REJECTED -> {
                    sAct = { api.cancelRequest(meId, other); refresh() }
                    sLabel = "요청삭제"
                }
                else -> Unit
            }

            RowView(
                otherId = other,
                stateForMe = stateForMe,
                primaryAction = pAct,
                primaryLabel = pLabel,
                secondaryAction = sAct,
                secondaryLabel = sLabel
            )
        }.sortedWith(
            compareBy<RowView> {
                when (it.stateForMe) {
                    RelationState.PENDING_RECV -> 0
                    RelationState.PENDING_SENT -> 1
                    RelationState.ACCEPTED     -> 2
                    RelationState.REJECTED     -> 3
                    else -> 4
                }
            }.thenBy { it.otherId }
        )

        _myRelations.value = result
    }

    fun sendRequest(to: String) {
        val target = to.trim()
        if (target.isEmpty() || target == meId) return
        api.sendRequest(meId, target)
        refresh()
    }
}

/* ───────── UI ───────── */
@Composable
fun RelationScreen(
    meId: String = "010-0000-0000",
    repo: FakeRelationRepository = remember { FakeRelationRepository(meId) }
) {
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("회원 공유(상호 승인)") }) }
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
                        scope.launch { repo.sendRequest(input) }
                        input = ""
                    },
                    enabled = input.trim().isNotEmpty()
                ) { Text("요청 보내기") }

                OutlinedButton(onClick = { repo.refresh() }) { Text("새로고침") }
            }

            Text("내 관계", style = MaterialTheme.typography.titleMedium)
            RelationList(repo)
        }
    }
}

@Composable
private fun RelationList(repo: FakeRelationRepository) {
    val list by repo.myRelations.collectAsState()

    if (list.isEmpty()) {
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(16.dp), contentAlignment = Alignment.CenterStart) {
                Text("관계가 없습니다. 상단에서 요청을 보내보세요.")
            }
        }
        return
    }

    val scope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(list, key = { it.otherId }) { row ->
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
                            RelationState.PENDING_SENT -> "내가 보낸 요청(상대 수락 대기)"
                            RelationState.ACCEPTED     -> "공유 중"
                            RelationState.REJECTED     -> "거절됨"
                            else                       -> ""
                        }
                        Text(stateLabel, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.secondaryAction?.let { act ->
                            OutlinedButton(onClick = { scope.launch { runCatching { act() } } }) {
                                Text(row.secondaryLabel.orEmpty())
                            }
                        }
                        row.primaryAction?.let { act ->
                            Button(onClick = { scope.launch { runCatching { act() } } }) {
                                Text(row.primaryLabel.orEmpty())
                            }
                        }
                    }
                }
            }
        }
    }
}
