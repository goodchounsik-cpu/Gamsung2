package com.gamsung2.relations.remote

import com.gamsung2.model.Relation
import com.gamsung2.model.RelationState

/** 실제 배포 시 Retrofit 인터페이스로 교체할 자리 */
interface RelationApi {
    suspend fun getMyRelations(meId: String): List<RelationDto>
    suspend fun sendRequest(body: SendRequestBody)
    suspend fun accept(relationId: String)
    suspend fun reject(relationId: String)
    suspend fun cancel(relationId: String)
}

/* ---------- DTO ---------- */
data class RelationDto(
    val id: String,
    val fromId: String,
    val toId: String,
    val state: RelationStateDto,
    val createdAt: Long
)

enum class RelationStateDto { NONE, PENDING_SENT, ACCEPTED, REJECTED }

data class SendRequestBody(
    val fromId: String,
    val toId: String
)

/* ---------- 매핑(서버 → 도메인) ---------- */
fun RelationDto.toModel(): Relation =
    Relation(
        id = id,
        fromId = fromId,
        toId = toId,
        state = when (state) {
            RelationStateDto.NONE         -> RelationState.NONE
            RelationStateDto.PENDING_SENT -> RelationState.PENDING_SENT
            RelationStateDto.ACCEPTED     -> RelationState.ACCEPTED
            RelationStateDto.REJECTED     -> RelationState.REJECTED
        },
        createdAt = createdAt
    )
