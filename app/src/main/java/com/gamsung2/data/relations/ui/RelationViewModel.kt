package com.gamsung2.data.relations.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.model.RelationRow
import com.gamsung2.model.RelationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 샘플 데이터로만 동작하는 ViewModel (DI 없이 빌드되도록).
 * 나중에 Repository 연결 시 생성자 주입만 추가하면 됩니다.
 */
class RelationViewModel : ViewModel() {

    private val _rows = MutableStateFlow<List<RelationRow>>(emptyList())
    val rows: StateFlow<List<RelationRow>> = _rows.asStateFlow()

    fun boot(meId: String) {
        // 실제에선 meId 저장 후 Repository에서 로드
        viewModelScope.launch {
            delay(100) // 로딩 느낌
            _rows.value = sampleRows
        }
    }

    fun refresh() {
        // 필요 시 새로고침 로직
        viewModelScope.launch {
            // TODO: Repository에서 다시 로드
        }
    }

    fun sendRequest(targetPhone: String) {
        viewModelScope.launch {
            // TODO: 요청 전송 후 refresh()
        }
    }

    private val sampleRows: List<RelationRow> = listOf(
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
