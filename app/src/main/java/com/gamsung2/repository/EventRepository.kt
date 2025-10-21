package com.gamsung2.repository

import com.gamsung2.data.local.EventDao
import com.gamsung2.data.local.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * EventDao 어댑터 레이어
 * - DAO는 문자열(date="YYYY-MM-DD", time="HH:mm")을 기대하므로,
 *   LocalDate/YearMonth를 편하게 쓸 수 있도록 변환 오버로드를 제공한다.
 */
class EventRepository(private val dao: EventDao) {

    private val df: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE // "YYYY-MM-DD"

    // =========================
    // 조회(문자열 API - DAO 직결)
    // =========================

    /** 특정 날짜(YYYY-MM-DD) 일정 스트림 */
    fun observeByDate(date: String): Flow<List<EventEntity>> =
        dao.observeByDate(date)

    /** 날짜 문자열 범위 [fromDate, toDate] 일정 스트림 */
    fun observeInRange(fromDate: String, toDate: String): Flow<List<EventEntity>> =
        dao.observeInRange(fromDate, toDate)

    /** 월 범위(YearMonth) 일정 스트림: 달력 화면용 (1일~말일) */
    fun observeByYearMonth(ym: YearMonth): Flow<List<EventEntity>> =
        dao.observeInRange(ym.atDay(1).toString(), ym.atEndOfMonth().toString())

    /** 특정 날짜 일정 개수(달력 도트/뱃지용) */
    fun countOnDate(date: String): Flow<Int> = dao.countOnDate(date)

    /** 제목/메모 키워드 검색 */
    fun search(query: String): Flow<List<EventEntity>> = dao.search(query)

    /** ✅ 단건 구독 (상세/편집 실시간 반영) */
    fun observeById(id: Long): Flow<EventEntity?> = dao.observeById(id)

    /** 단건 조회 (1회성) */
    suspend fun getById(id: Long): EventEntity? = dao.getById(id)

    /** 날짜별 개수 맵(그룹화 결과) */
    fun countsByRange(fromDate: String, toDate: String) =
        dao.countsByRange(fromDate, toDate)

    // =========================
    // 조회(LocalDate/YearMonth 편의 오버로드)
    // =========================

    fun observeByDate(date: LocalDate): Flow<List<EventEntity>> =
        observeByDate(df.format(date))

    fun observeInRange(from: LocalDate, to: LocalDate): Flow<List<EventEntity>> =
        observeInRange(df.format(from), df.format(to))

    fun countsByRange(from: LocalDate, to: LocalDate) =
        countsByRange(df.format(from), df.format(to))

    fun countOnDate(date: LocalDate): Flow<Int> =
        countOnDate(df.format(date))

    // =========================
    // 저장 / 수정
    // =========================

    /**
     * 일정 추가(업서트)
     * - memo 가 null 이면 "" 로 치환 (Entity는 non-null)
     * - allDay=true 이면 startTime/endTime 은 null 로 저장
     */
    suspend fun add(
        title: String,
        date: String,                 // "YYYY-MM-DD"
        memo: String? = null,
        allDay: Boolean = true,
        startTime: String? = null,    // "HH:mm"
        endTime: String? = null       // "HH:mm"
    ) {
        val entity = EventEntity(
            title     = title,
            date      = date,
            memo      = memo ?: "",
            allDay    = allDay,
            startTime = if (allDay) null else startTime,
            endTime   = if (allDay) null else endTime
        )
        upsert(entity)
    }

    /** LocalDate 버전 */
    suspend fun add(
        title: String,
        date: LocalDate,
        memo: String? = null,
        allDay: Boolean = true,
        startTime: String? = null,
        endTime: String? = null
    ) = add(title, df.format(date), memo, allDay, startTime, endTime)

    /**
     * ⏰ AM/PM(12시간제)로 전달받아 저장하는 오버로드
     * 예) add12h(title, date, "메모", false, 9, 5, true, 10, 0, true) -> 09:05~10:00
     */
    suspend fun add12h(
        title: String,
        date: String,
        memo: String? = null,
        allDay: Boolean = false,
        startHour12: Int? = null, startMinute: Int? = null, startIsAm: Boolean? = null,
        endHour12: Int? = null, endMinute: Int? = null, endIsAm: Boolean? = null
    ) {
        if (allDay) {
            add(title, date, memo, true, null, null)
            return
        }
        val s = toHHmm(startHour12!!, startMinute!!, startIsAm!!)
        val e = toHHmm(endHour12!!, endMinute!!, endIsAm!!)
        add(title, date, memo, false, s, e)
    }

    /** 수정(업서트) – id가 있는 엔티티를 그대로 저장 */
    suspend fun update(e: EventEntity) = upsert(e)

    /** 직접 엔티티 전달 업서트 (Room 2.5 미만 폴백 포함) */
    suspend fun upsert(e: EventEntity) {
        runCatching { dao.upsert(e) }    // Room 2.5+ @Upsert
            .onFailure { dao.insert(e) } // 구버전 폴백
    }

    // =========================
    // 삭제
    // =========================
    suspend fun delete(e: EventEntity) = dao.delete(e)
    suspend fun deleteById(id: Long)   = dao.deleteById(id)
    suspend fun deleteByDate(date: String) = dao.deleteByDate(date)

    // =========================
    // 유틸 (12h/24h 변환)
    // =========================

    /** 12시간제(AM/PM) -> "HH:mm" */
    fun toHHmm(hour12: Int, minute: Int, isAm: Boolean): String {
        val h24 = when {
            isAm && hour12 == 12 -> 0
            !isAm && hour12 == 12 -> 12
            isAm -> hour12
            else -> hour12 + 12
        }
        return String.format(Locale.US, "%02d:%02d", h24, minute)
    }

    /** "HH:mm" -> Triple(hour12, minute, isAm) */
    fun fromHHmm(hhmm: String): Triple<Int, Int, Boolean> {
        val (h, m) = hhmm.split(":").map { it.toInt() }
        val isAm = h < 12
        val hour12 = when (val mod = h % 12) { 0 -> 12; else -> mod }
        return Triple(hour12, m, isAm)
    }
}
