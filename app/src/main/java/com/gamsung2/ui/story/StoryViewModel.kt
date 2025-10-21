package com.gamsung2.ui.story

import androidx.lifecycle.ViewModel
import com.gamsung2.domain.plan.Bucket
import com.gamsung2.domain.plan.BucketWish
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.domain.plan.TripStory
import com.gamsung2.domain.plan.VisitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 여행 코스/버킷 상태를 관리하는 ViewModel (단일 정의본)
 *
 * - 버킷 최대 100개
 * - 도메인 모델의 nullable 리스트(courses/foods/lodgings)에 대한 null-안전 처리
 * - 좌표 캐시 및 코스 순서 추천 유틸 제공
 */
class StoryViewModel : ViewModel() {

    /* ---------- 설정 ---------- */
    private val BUCKET_LIMIT = 100

    /* ---------- 상태 ---------- */
    private val _story = MutableStateFlow(TripStory())
    val story = _story.asStateFlow()

    private val _bucket = MutableStateFlow(BucketWish())
    val bucket = _bucket.asStateFlow()

    /** 코스 순서(1코스, 2코스… 표시에 사용). 값은 placeId 리스트 */
    private val _courseOrder = MutableStateFlow<List<String>>(emptyList())
    val courseOrder = _courseOrder.asStateFlow()

    /** 담은 장소의 좌표 캐시 (코스 추천/지도 표기용) */
    private val coord = mutableMapOf<String, Pair<Double, Double>>()

    /* ---------- 읽기 유틸 ---------- */

    fun bucketCount(): Int = _bucket.value.items.size
    fun bucketRemaining(): Int = (BUCKET_LIMIT - bucketCount()).coerceAtLeast(0)
    fun isInBucket(id: String): Boolean = _bucket.value.items.any { it.id == id }

    fun isInStory(bucket: Bucket, id: String): Boolean = when (bucket) {
        Bucket.COURSE  -> (_story.value.courses  ?: emptyList()).any { it.id == id }
        Bucket.FOOD    -> (_story.value.foods    ?: emptyList()).any { it.id == id }
        Bucket.LODGING -> (_story.value.lodgings ?: emptyList()).any { it.id == id }
    }

    fun missionDoneCount(): Int =
        listOf(
            _story.value.courses  ?: emptyList(),
            _story.value.foods    ?: emptyList(),
            _story.value.lodgings ?: emptyList()
        ).flatten().count { it.status == VisitStatus.DONE }

    fun missionDoneCount(bucket: Bucket): Int = when (bucket) {
        Bucket.COURSE  -> (_story.value.courses  ?: emptyList()).count { it.status == VisitStatus.DONE }
        Bucket.FOOD    -> (_story.value.foods    ?: emptyList()).count { it.status == VisitStatus.DONE }
        Bucket.LODGING -> (_story.value.lodgings ?: emptyList()).count { it.status == VisitStatus.DONE }
    }

    /* ---------- 쓰기 유틸 (스토리) ---------- */

    fun addToStory(item: PlanItem) = _story.update { cur ->
        when (item.bucket) {
            Bucket.COURSE  -> cur.copy(courses  = ((cur.courses  ?: emptyList()) + item).distinctBy { it.id })
            Bucket.FOOD    -> cur.copy(foods    = ((cur.foods    ?: emptyList()) + item).distinctBy { it.id })
            Bucket.LODGING -> cur.copy(lodgings = ((cur.lodgings ?: emptyList()) + item).distinctBy { it.id })
        }
    }

    fun removeFromStory(bucket: Bucket, id: String) = _story.update { cur ->
        fun List<PlanItem>?.rm() = (this ?: emptyList()).filterNot { it.id == id }
        when (bucket) {
            Bucket.COURSE  -> cur.copy(courses  = cur.courses.rm())
            Bucket.FOOD    -> cur.copy(foods    = cur.foods.rm())
            Bucket.LODGING -> cur.copy(lodgings = cur.lodgings.rm())
        }
    }

    fun toggleStory(item: PlanItem): Boolean =
        if (isInStory(item.bucket, item.id)) { removeFromStory(item.bucket, item.id); false }
        else { addToStory(item); true }

    /* ---------- 쓰기 유틸 (버킷) ---------- */

    fun addToBucket(item: PlanItem) = _bucket.update { cur ->
        cur.copy(items = (cur.items + item).distinctBy { it.id })
    }

    fun removeFromBucket(id: String) = _bucket.update { cur ->
        cur.copy(items = cur.items.filterNot { it.id == id })
    }

    fun tryAddToBucket(item: PlanItem): Boolean {
        val cur = _bucket.value
        if (cur.items.any { it.id == item.id }) return false
        if (cur.items.size >= BUCKET_LIMIT) return false
        _bucket.update { it.copy(items = it.items + item) }
        return true
    }

    fun toggleBucket(item: PlanItem): Boolean =
        if (isInBucket(item.id)) { removeFromBucket(item.id); false } else { tryAddToBucket(item) }

    /* ---------- 미션 처리 ---------- */

    fun markDone(itemId: String, bucket: Bucket, photoUris: List<String> = emptyList()) =
        _story.update { cur ->
            fun List<PlanItem>.mark() = map {
                if (it.id == itemId) it.copy(
                    status = VisitStatus.DONE,
                    photos = (it.photos + photoUris).distinct(),
                    visitedAt = System.currentTimeMillis()
                ) else it
            }
            when (bucket) {
                Bucket.COURSE  -> cur.copy(courses  = (cur.courses  ?: emptyList()).mark())
                Bucket.FOOD    -> cur.copy(foods    = (cur.foods    ?: emptyList()).mark())
                Bucket.LODGING -> cur.copy(lodgings = (cur.lodgings ?: emptyList()).mark())
            }
        }

    /* ---------- 좌표 / 코스 순서 유틸 ---------- */

    /** 장소 좌표 저장(코스 추천/지도 표기용) */
    fun setCoord(id: String, lat: Double?, lon: Double?) {
        if (lat != null && lon != null) coord[id] = lat to lon
    }
    fun getCoord(id: String): Pair<Double, Double>? = coord[id]

    /** 외부에서 순서를 지정(예: 추천 결과 반영) */
    fun setCourseOrder(ids: List<String>) {
        _courseOrder.value = ids.distinct()
    }

    /** 현재 순서에 맞춘 (번호, 항목) 리스트 반환 */
    fun orderedCourses(): List<Pair<Int, PlanItem>> {
        val all = _story.value.courses ?: emptyList()
        if (all.isEmpty()) return emptyList()
        val byId = all.associateBy { it.id }

        val ordered = _courseOrder.value.mapNotNull { byId[it] }
        val rest = all.filter { it.id !in _courseOrder.value }

        return (ordered + rest).mapIndexed { idx, item -> (idx + 1) to item }
    }

    /** 좌표 기반 최근접 탐욕법으로 코스 순서 추천 */
    fun recommendCourseOrder(): List<String> {
        val items = _story.value.courses ?: return emptyList()
        val pts = items.mapNotNull { item -> getCoord(item.id)?.let { item.id to it } }.toMap()
        if (pts.size <= 1) return items.map { it.id }

        val remaining = pts.toMutableMap()
        val start = remaining.keys.first()
        val path = mutableListOf(start)
        var cur = start
        remaining.remove(cur)

        while (remaining.isNotEmpty()) {
            val (next, _) = remaining.minByOrNull { (_, p) -> dist(pts[cur]!!, p) }!!
            path += next
            remaining.remove(next)
            cur = next
        }
        return path + items.filter { it.id !in path }.map { it.id }
    }

    private fun dist(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {
        val (x1, y1) = a; val (x2, y2) = b
        val dx = x1 - x2; val dy = y1 - y2
        return dx * dx + dy * dy
    }

    /* ---------- 초기화 ---------- */

    fun clearStory()  = _story.update { TripStory() }
    fun clearBucket() = _bucket.update { BucketWish() }
}
