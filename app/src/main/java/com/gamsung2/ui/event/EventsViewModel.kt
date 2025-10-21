// app/src/main/java/com/gamsung2/ui/event/EventsViewModel.kt
package com.gamsung2.ui.event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.local.AppDatabase
import com.gamsung2.data.local.EventEntity
import com.gamsung2.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class EventsViewModel(app: Application) : AndroidViewModel(app) {

    // ✅ Application이 아니라 DAO를 주입해야 합니다.
    private val repo = EventRepository(
        AppDatabase.getInstance(app).eventDao()
    )

    private val _month = MutableStateFlow(YearMonth.from(LocalDate.now()))
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    /** 월 그리드 범위(5주 35칸) */
    private val range: StateFlow<Pair<LocalDate, LocalDate>> =
        _month.map { ym ->
            val first = ym.atDay(1)
            val sundayShift = (first.dayOfWeek.value % 7).toLong()
            val start = first.minusDays(sundayShift)
            start to start.plusDays(34)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            run {
                val ym = YearMonth.from(LocalDate.now())
                val first = ym.atDay(1)
                val shift = (first.dayOfWeek.value % 7).toLong()
                val start = first.minusDays(shift)
                start to start.plusDays(34)
            }
        )

    /** 월 범위 내 모든 일정 */
    val eventsInRange: StateFlow<List<EventEntity>> =
        range.flatMapLatest { (s, e) -> repo.observeInRange(s, e) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 날짜별 개수 맵(달력 점 표시용) */
    val countMap: StateFlow<Map<String, Int>> =
        range.flatMapLatest { (s, e) -> repo.countsByRange(s, e) }
            .map { list -> list.associate { it.date to it.cnt } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun setMonth(ym: YearMonth) { _month.value = ym }

    fun eventsOn(date: LocalDate): List<EventEntity> =
        eventsInRange.value.filter { it.date == date.toString() }

    fun hasEvents(date: LocalDate): Boolean =
        (countMap.value[date.toString()] ?: 0) > 0

    /** 새 일정 추가 */
    fun addNew(
        date: LocalDate,
        title: String,
        allDay: Boolean,
        startHHmm: String?,
        endHHmm: String?,
        memo: String?
    ) = viewModelScope.launch {
        repo.upsert(
            EventEntity(
                date = date.toString(),
                title = title,
                memo = memo,
                allDay = allDay,
                startTime = if (allDay) null else startHHmm,
                endTime = if (allDay) null else endHHmm
            )
        )
    }

    /** 일정 수정/삭제 */
    fun update(e: EventEntity) = viewModelScope.launch { repo.upsert(e) }
    fun delete(e: EventEntity) = viewModelScope.launch { repo.delete(e) }
}
