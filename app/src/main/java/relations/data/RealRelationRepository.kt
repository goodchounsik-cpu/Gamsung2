package com.gamsung2.relations.data

import com.gamsung2.model.Relation
import com.gamsung2.model.RelationRow
import com.gamsung2.model.RelationState
import com.gamsung2.relations.remote.RelationApi
import com.gamsung2.relations.remote.SendRequestBody
import com.gamsung2.relations.remote.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class RealRelationRepository @Inject constructor(
    private val api: RelationApi
) : RelationRepository {

    private var meId: String = ""
    private val _rows = MutableStateFlow<List<RelationRow>>(emptyList())
    override val rows: StateFlow<List<RelationRow>> = _rows

    override suspend fun boot(meId: String) {
        this.meId = meId
        refresh()
    }

    override suspend fun refresh() {
        val models: List<Relation> = api.getMyRelations(meId).map { it.toModel() }
        _rows.value = models.map { it.toRowFor(meId) }
    }

    override suspend fun sendRequest(targetPhone: String) {
        val to = targetPhone.trim()
        if (to.isEmpty()) return
        api.sendRequest(SendRequestBody(fromId = meId, toId = to))
        refresh()
    }
}

/* ---------- 도메인 → UI Row 매핑 ---------- */
private fun Relation.toRowFor(meId: String): RelationRow {
    val other = if (fromId == meId) toId else fromId
    val stateForMe =
        if (state == RelationState.PENDING_SENT && toId == meId) {
            // 내가 받은 보류 상태
            RelationState.PENDING_RECV
        } else {
            state
        }
    return RelationRow(
        otherId = other,
        stateForMe = stateForMe,
        // 버튼 라벨은 화면에서 상태에 따라 표시하므로 비워둠
        primaryLabel = null,
        secondaryLabel = null
    )
}
