// app/src/main/java/com/gamsung2/viewmodel/EventViewModel.kt
package com.gamsung2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.local.AppDatabase
import com.gamsung2.data.local.EventEntity
import com.gamsung2.repository.EventRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class EventViewModel(
    private val repo: EventRepository
) : ViewModel() {

    // ---------- UI one-shot events ----------
    sealed class UiEvent { data class Message(val text: String) : UiEvent() }
    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents = _uiEvents.asSharedFlow()

    // ---------- States ----------
    private val _selectedDate = MutableStateFlow(LocalDate.now().toString()) // "yyyy-MM-dd"
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    /** 선택 날짜의 일정 리스트 */
    val events: StateFlow<List<EventEntity>> =
        _selectedDate
            .flatMapLatest { date ->
                if (date.isBlank()) flowOf(emptyList())
                else repo.observeByDate(date)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 달력 도트/뱃지용: 월 범위 내 날짜별 개수 맵 */
    val monthBadgeCounts: StateFlow<Map<String, Int>> =
        _currentMonth
            .flatMapLatest { ym -> repo.observeByYearMonth(ym) }
            .map { list -> list.groupBy { it.date }.mapValues { (_, v) -> v.size } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ---------- Actions ----------
    fun setDate(isoDate: String) { _selectedDate.value = isoDate }
    fun setYearMonth(yearMonth: YearMonth) { _currentMonth.value = yearMonth }

    /** 기존 문자열 버전 (호환 유지) */
    fun add(
        title: String,
        date: String = selectedDate.value,
        memo: String? = null,
        allDay: Boolean = true,
        startTime: String? = null,   // "HH:mm"
        endTime: String? = null      // "HH:mm"
    ) {
        viewModelScope.launch {
            repo.add(
                title = title,
                date = date,
                memo = memo,
                allDay = allDay,
                startTime = startTime,
                endTime = endTime
            )
            _uiEvents.tryEmit(UiEvent.Message("일정이 추가되었어요."))
        }
    }

    /** ✅ 오버로드 #1: LocalDate/LocalTime 버전 (시계 다이얼 결과 바로 저장) */
    fun addEvent(
        title: String,
        date: LocalDate,
        start: LocalTime?,
        end: LocalTime?,
        memo: String?
    ) {
        add(
            title = title,
            date = date.toString(),
            memo = memo,
            allDay = (start == null && end == null),
            startTime = start.toHhmm(),
            endTime = end.toHhmm()
        )
    }

    /** ✅ 오버로드 #2: 문자열 인자도 addEvent 이름으로 통일 */
    fun addEvent(
        title: String,
        date: String = selectedDate.value,
        memo: String? = null,
        allDay: Boolean = true,
        startTime: String? = null,
        endTime: String? = null
    ) {
        add(title, date, memo, allDay, startTime, endTime)
    }

    /** 엔티티 전체 교체(수정) */
    fun update(
        origin: EventEntity,
        title: String,
        memo: String?,
        allDay: Boolean,
        startTime: String?,
        endTime: String?
    ) {
        viewModelScope.launch {
            repo.upsert(
                origin.copy(
                    title = title,
                    memo = memo ?: "",
                    allDay = allDay,
                    startTime = if (allDay) null else startTime,
                    endTime   = if (allDay) null else endTime
                )
            )
            _uiEvents.tryEmit(UiEvent.Message("일정이 수정되었어요."))
        }
    }

    /** ✅ id 기반 부분 수정 (편집/상세에서 사용) */
    fun updateEvent(
        id: Long,
        title: String,
        start: LocalTime?,
        end: LocalTime?,
        memo: String?
    ) {
        viewModelScope.launch {
            val origin = repo.getById(id) ?: return@launch
            val allDay = (start == null && end == null)
            repo.upsert(
                origin.copy(
                    title = title,
                    memo = memo ?: "",
                    allDay = allDay,
                    startTime = if (allDay) null else start.toHhmm(),
                    endTime   = if (allDay) null else end.toHhmm()
                )
            )
            _uiEvents.tryEmit(UiEvent.Message("일정이 수정되었어요."))
        }
    }

    /** 엔티티 삭제 */
    fun delete(e: EventEntity) {
        viewModelScope.launch {
            repo.delete(e)
            _uiEvents.tryEmit(UiEvent.Message("일정이 삭제되었어요."))
        }
    }

    /** ✅ id로 삭제 */
    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            // 레포에 deleteById가 있으니 바로 사용해도 됨
            repo.deleteById(id)
            _uiEvents.tryEmit(UiEvent.Message("일정이 삭제되었어요."))
        }
    }

    /** ✅ 단건 실시간 구독 (상세/편집에서 사용) */
    fun observeEvent(id: Long) = repo.observeById(id)

    fun jumpToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today.toString()
        _currentMonth.value = YearMonth.from(today)
    }

    // ---------- Helpers ----------
    private fun LocalTime?.toHhmm(): String? =
        this?.let { "%02d:%02d".format(it.hour, it.minute) }
}

/** ViewModel Factory */
@Suppress("UNCHECKED_CAST")
fun eventVmFactory(context: Context): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            val repo = EventRepository(db.eventDao())
            return EventViewModel(repo) as T
        }
    }
