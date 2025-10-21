package com.gamsung2.util

import java.time.LocalDate

/**
 * 간단한 대한민국 공휴일/명절 라벨 제공 유틸.
 *
 * - 양력 고정 공휴일은 연도 무관하게 처리
 * - 음력 기반(설/추석/부처님오신날)은 데모용 샘플 매핑(2024~2026) 포함
 *   → 실제 서비스에서는 공공데이터 포털/사내 캘린더/ICS 등으로 대체 권장
 *
 * 사용 예)
 *   val labels = HolidayProvider.getHolidaysInRange(weekStart, 7)  // Map<LocalDate, String>
 *   val label: String? = HolidayProvider.isHoliday(LocalDate.now())
 */
object HolidayProvider {

    /** 양력 고정 공휴일 (month-day -> label) */
    private val fixedSolar: Map<String, String> = mapOf(
        "01-01" to "신정",
        "03-01" to "삼일절",
        "05-05" to "어린이날",
        "06-06" to "현충일",
        "08-15" to "광복절",
        "10-03" to "개천절",
        "10-09" to "한글날",
        "12-25" to "성탄절"
    )

    /**
     * 데모용 음력 기반 공휴일/명절 샘플 (yyyy-MM-dd -> label).
     * - 대체공휴일/지역별 차이는 반영 안 함
     * - 필요하면 연도를 확장하세요.
     */
    private val lunarSample: Map<String, String> = mapOf(
        // 2024
        "2024-02-09" to "설연휴", "2024-02-10" to "설날", "2024-02-11" to "설연휴",
        "2024-05-15" to "부처님오신날",
        "2024-09-16" to "추석연휴", "2024-09-17" to "추석", "2024-09-18" to "추석연휴",

        // 2025
        "2025-01-28" to "설연휴", "2025-01-29" to "설날", "2025-01-30" to "설연휴",
        "2025-05-05" to "어린이날",      // (양력 고정과 중복 가능)
        "2025-10-06" to "추석연휴", "2025-10-07" to "추석", "2025-10-08" to "추석연휴",

        // 2026 (예시)
        "2026-02-16" to "설연휴", "2026-02-17" to "설날", "2026-02-18" to "설연휴",
        "2026-05-24" to "부처님오신날",
        "2026-09-24" to "추석연휴", "2026-09-25" to "추석", "2026-09-26" to "추석연휴"
    )

    /**
     * [start] 부터 [days]일 범위(양끝 포함)의 공휴일/명절 라벨 맵을 반환.
     * - 키: LocalDate, 값: 라벨
     * - 같은 날짜에 양력/음력 라벨이 겹치면 음력(명절) 라벨을 우선
     */
    fun getHolidaysInRange(start: LocalDate, days: Int): Map<LocalDate, String> {
        require(days > 0) { "days must be > 0" }
        val endInclusive = start.plusDays(days.toLong() - 1)
        val out = linkedMapOf<LocalDate, String>()

        var d = start
        while (!d.isAfter(endInclusive)) {
            // 1) 양력 고정
            fixedSolar["%02d-%02d".format(d.monthValue, d.dayOfMonth)]?.let { label ->
                out.putIfAbsent(d, label)
            }
            // 2) 샘플 음력 (존재 시 덮어쓰기 → 명절 우선)
            lunarSample[d.toString()]?.let { label ->
                out[d] = label
            }
            d = d.plusDays(1)
        }
        return out
    }

    /** 해당 날짜가 공휴일/명절이면 라벨을, 아니면 null 반환 */
    fun isHoliday(date: LocalDate): String? {
        val solar = fixedSolar["%02d-%02d".format(date.monthValue, date.dayOfMonth)]
        val lunar = lunarSample[date.toString()]
        return lunar ?: solar
    }

    /**
     * 특정 월의 공휴일/명절 라벨 맵.
     * UI에서 월 달력 표시에 유용.
     */
    fun getHolidaysForMonth(year: Int, month: Int): Map<LocalDate, String> {
        val first = LocalDate.of(year, month, 1)
        val days = first.lengthOfMonth()
        return getHolidaysInRange(first, days)
    }
}
