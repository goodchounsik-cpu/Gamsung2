// app/src/main/java/com/gamsung2/relations/data/FakeRelationRepository.kt
package com.gamsung2.relations.data

import com.gamsung2.model.RelationRow          // ✅ 모델 패키지 올바르게
import com.gamsung2.model.RelationState        // ✅ 모델 패키지 올바르게
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRelationRepository @Inject constructor() : RelationRepository {

    private val _rows = MutableStateFlow<List<RelationRow>>(emptyList())
    override val rows: StateFlow<List<RelationRow>> = _rows

    override suspend fun boot(meId: String) {
        // 데모용 더미 데이터
        delay(100)
        _rows.value = listOf(
            RelationRow(
                otherId = "010-1234-5678",
                stateForMe = RelationState.PENDING_RECV,
                primaryLabel = "수락",
                secondaryLabel = "거절"
            ),
            RelationRow(
                otherId = "010-9876-5432",
                stateForMe = RelationState.PENDING_SENT,
                primaryLabel = "요청취소"
            )
        )
    }

    override suspend fun refresh() { /* no-op (데모) */ }

    override suspend fun sendRequest(targetPhone: String) { /* no-op (데모) */ }
}
