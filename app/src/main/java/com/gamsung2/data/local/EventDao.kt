// app/src/main/java/com/gamsung2/data/local/EventDao.kt
package com.gamsung2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * events 테이블 DAO
 *
 * 정렬 규칙
 *  - 날짜(필요시)
 *  - 종일(allDay=true) 먼저
 *  - 시작시간(startTime) 오름차순 (종일 NULL은 '24:00'으로 치환해 맨 뒤로)
 *  - id DESC (최근 것이 위에)
 *
 * 시간 포맷
 *  - startTime / endTime: "HH:mm" (zero-padded)
 *  - 종일: startTime/endTime = NULL
 */
@Dao
interface EventDao {

    // =========================
    // 조회 스트림
    // =========================

    /**
     * 특정 날짜 일정 스트림
     */
    @Query(
        """
        SELECT * FROM events
        WHERE date = :date
        ORDER BY
            allDay DESC,
            CASE WHEN startTime IS NULL THEN '24:00' ELSE startTime END ASC,
            id DESC
        """
    )
    fun observeByDate(date: String): Flow<List<EventEntity>>

    /**
     * [fromDate, toDate] 범위 일정 스트림 (월/주 달력용)
     */
    @Query(
        """
        SELECT * FROM events
        WHERE date BETWEEN :fromDate AND :toDate
        ORDER BY
            date ASC,
            allDay DESC,
            CASE WHEN startTime IS NULL THEN '24:00' ELSE startTime END ASC,
            id DESC
        """
    )
    fun observeInRange(fromDate: String, toDate: String): Flow<List<EventEntity>>

    /**
     * 달력 뱃지 점 표시에 쓰는 날짜별 개수 맵 (그룹화)
     * - return: [ (date, cnt), ... ]
     */
    @Query(
        """
        SELECT date AS date, COUNT(*) AS cnt
        FROM events
        WHERE date BETWEEN :fromDate AND :toDate
        GROUP BY date
        ORDER BY date ASC
        """
    )
    fun countsByRange(fromDate: String, toDate: String): Flow<List<DateCount>>

    /**
     * 단일 날짜 개수 (간단 케이스)
     */
    @Query("SELECT COUNT(*) FROM events WHERE date = :date")
    fun countOnDate(date: String): Flow<Int>

    /**
     * 키워드 검색 (제목/메모)
     * - case-insensitive
     */
    @Query(
        """
        SELECT * FROM events
        WHERE title LIKE '%' || :query || '%' ESCAPE '\' 
           OR memo  LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY
            date ASC,
            allDay DESC,
            CASE WHEN startTime IS NULL THEN '24:00' ELSE startTime END ASC,
            id DESC
        """
    )
    fun search(query: String): Flow<List<EventEntity>>

    /**
     * ✅ 단건 구독 (상세/편집 실시간 반영)
     */
    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<EventEntity?>

    /**
     * 단건 조회 (1회성)
     */
    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EventEntity?

    // =========================
    // 쓰기
    // =========================

    /** 삽입 (충돌 시 교체) — 호환용 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: EventEntity)

    /** 업서트 (Room 2.5+) — 최신 권장 */
    @Upsert
    suspend fun upsert(e: EventEntity)

    /** 삭제(엔티티) */
    @Delete
    suspend fun delete(e: EventEntity)

    /** 삭제(아이디) */
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 특정 날짜 전체 삭제 */
    @Query("DELETE FROM events WHERE date = :date")
    suspend fun deleteByDate(date: String)
}

/**
 * 날짜별 개수 프로젝션 (countsByRange 용)
 */
data class DateCount(
    val date: String,
    val cnt: Int
)
