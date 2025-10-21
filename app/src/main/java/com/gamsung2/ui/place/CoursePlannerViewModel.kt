package com.gamsung2.ui.place

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/* ---------- UI 모델 ---------- */
data class PlannerFilters(
    val seed: String = "",
    val days: Int = 2,
    val region: String = "서울"
)

data class PlannerMetrics(
    val totalTimeMin: Int = 0,
    val moveTimeMin: Int = 0,
    val budget: Int = 0
)

data class PlanItemLite(
    val id: String,
    val title: String,
    val category: String // "코스" / "식당" / "숙소"
)

data class ItineraryDay(val items: List<PlanItemLite> = emptyList())
data class Itinerary(val days: List<ItineraryDay> = List(2) { ItineraryDay() })

data class CoursePlannerUi(
    val filters: PlannerFilters = PlannerFilters(),
    val itinerary: Itinerary = Itinerary(),
    val candidates: List<PlanItemLite> = emptyList(),
    val metrics: PlannerMetrics = PlannerMetrics(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CoursePlannerViewModel @Inject constructor() : ViewModel() {

    private val _ui = MutableStateFlow(CoursePlannerUi())
    val ui: StateFlow<CoursePlannerUi> = _ui

    /* ---------- 사용자 입력 ---------- */
    fun updateSeed(seed: String) = _ui.update { it.copy(filters = it.filters.copy(seed = seed)) }

    fun updateDays(days: Int) {
        val d = days.coerceIn(1, 7)
        _ui.update { prev ->
            val old = prev.itinerary.days
            val nextDays = when {
                d > old.size -> old + List(d - old.size) { ItineraryDay() }
                d < old.size -> old.take(d)
                else -> old
            }
            prev.copy(
                filters = prev.filters.copy(days = d),
                itinerary = prev.itinerary.copy(days = nextDays)
            )
        }
        recomputeMetrics()
    }

    fun updateRegion(region: String) =
        _ui.update { it.copy(filters = it.filters.copy(region = region)) }

    /* ---------- 후보 생성/자동 구성 ---------- */
    fun fetchCandidates() {
        _ui.update { it.copy(loading = true, error = null) }

        // 더미 후보 생성 (키워드/지역 반영)
        val seed = _ui.value.filters.seed.ifBlank { "여행지" }
        val region = _ui.value.filters.region
        val categories = listOf("코스", "식당", "숙소")

        val list = (1..20).map { idx ->
            val cat = categories[idx % categories.size]
            PlanItemLite(
                id = "${cat}_${idx}",
                title = "$region $seed $cat #$idx",
                category = cat
            )
        }

        _ui.update { it.copy(candidates = list, loading = false) }
    }

    fun autoAssemble() {
        val days = _ui.value.filters.days
        val pool = if (_ui.value.candidates.isEmpty()) {
            fetchCandidates()
            _ui.value.candidates
        } else _ui.value.candidates

        val rnd = Random(0xC0FFEE)
        val perDay = 4
        val newDays = (0 until days).map { day ->
            val picks = List(perDay) { pool[rnd.nextInt(pool.size)] }
            ItineraryDay(items = picks)
        }
        _ui.update { it.copy(itinerary = Itinerary(newDays)) }
        recomputeMetrics()
    }

    fun addToDay(dayIdx: Int, item: PlanItemLite) {
        _ui.update { prev ->
            val days = prev.itinerary.days.toMutableList()
            if (dayIdx in days.indices) {
                val added = days[dayIdx].items + item
                days[dayIdx] = days[dayIdx].copy(items = added)
            }
            prev.copy(itinerary = prev.itinerary.copy(days = days))
        }
        recomputeMetrics()
    }

    fun removeItem(dayIdx: Int, id: String) {
        _ui.update { prev ->
            val days = prev.itinerary.days.toMutableList()
            if (dayIdx in days.indices) {
                val filtered = days[dayIdx].items.filterNot { it.id == id }
                days[dayIdx] = days[dayIdx].copy(items = filtered)
            }
            prev.copy(itinerary = prev.itinerary.copy(days = days))
        }
        recomputeMetrics()
    }

    fun savePlan() {
        // TODO: 실제 저장 연동
    }

    /* ---------- 메트릭 재계산(오버로드 모호성 회피 버전) ---------- */
    private fun recomputeMetrics() {
        val days = _ui.value.itinerary.days

        // 총 소요시간: 아이템 1개당 90분 가정
        val totalCount = days.fold(0) { acc, d -> acc + d.items.size }
        val total = totalCount * 90

        // 이동시간: 아이템 사이 이동 20분 가정
        val move = ((totalCount.coerceAtLeast(1) - 1) * 20)

        // 예산: 숙소 120,000 / 식당 15,000
        val budget = days.fold(0) { acc, d ->
            acc + d.items.fold(0) { acc2, it ->
                acc2 + when (it.category) {
                    "숙소" -> 120_000
                    "식당" -> 15_000
                    else -> 0
                }
            }
        }

        _ui.update { it.copy(metrics = PlannerMetrics(total, move, budget)) }
    }
}
