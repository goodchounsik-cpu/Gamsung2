package com.gamsung2.relations.remote

import kotlinx.coroutines.delay
import java.util.UUID

/**
 * 테스트/개발용 인메모리 API.
 * 실제 배포에서는 Retrofit 구현으로 교체하세요.
 */
class FakeRelationApi : RelationApi {

    /** key = setOf(a, b) 한 쌍만 보관 */
    private val map = LinkedHashMap<Set<String>, RelationDto>()

    override suspend fun getMyRelations(meId: String): List<RelationDto> {
        delay(50) // 네트워크 흉내
        return map.values.filter { it.fromId == meId || it.toId == meId }
    }

    override suspend fun sendRequest(body: SendRequestBody) {
        delay(50)
        val k = setOf(body.fromId, body.toId)
        if (!map.containsKey(k)) {
            map[k] = RelationDto(
                id = UUID.randomUUID().toString(),
                fromId = body.fromId,
                toId = body.toId,
                state = RelationStateDto.PENDING_SENT,
                createdAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun accept(relationId: String) {
        delay(50)
        map.replaceIf({ it.id == relationId }) { it.copy(state = RelationStateDto.ACCEPTED) }
    }

    override suspend fun reject(relationId: String) {
        delay(50)
        map.replaceIf({ it.id == relationId }) { it.copy(state = RelationStateDto.REJECTED) }
    }

    override suspend fun cancel(relationId: String) {
        delay(50)
        val entry = map.entries.find { it.value.id == relationId } ?: return
        map.remove(entry.key)
    }

    private inline fun MutableMap<Set<String>, RelationDto>.replaceIf(
        pred: (RelationDto) -> Boolean,
        xform: (RelationDto) -> RelationDto
    ) {
        val key = entries.firstOrNull { pred(it.value) }?.key ?: return
        this[key] = xform(this[key]!!)
    }
}
