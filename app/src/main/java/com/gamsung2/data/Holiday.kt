// app/src/main/java/com/gamsung2/data/Holiday.kt
package com.gamsung2.data

import java.time.*

/** 양력/음력 구분 */
enum class HolidayType { SOLAR, LUNAR }

/** 표시 범주: 공휴일(빨간날), 국경일(국기게양일), 기념일(빨간날 아님) */
enum class HolidayCategory { PUBLIC_HOLIDAY, NATIONAL_DAY, COMMEMORATION }

/** 한국 달력용 모델 (UI 의존성 없음) */
data class Holiday(
    val date: LocalDate,
    val name: String,
    val type: HolidayType,
    val isPublic: Boolean = true,
    val category: HolidayCategory = HolidayCategory.PUBLIC_HOLIDAY
)

/* ----------------------- 고정 양력/국경일/기념일 ----------------------- */

private fun solarHolidays(year: Int): List<Holiday> {
    val y = Year.of(year)
    return listOf(
        Holiday(y.atMonth(Month.JANUARY).atDay(1),   "신정",     HolidayType.SOLAR, true,  HolidayCategory.PUBLIC_HOLIDAY),
        Holiday(y.atMonth(Month.MARCH).atDay(1),     "삼일절",   HolidayType.SOLAR, true,  HolidayCategory.NATIONAL_DAY),
        Holiday(y.atMonth(Month.MAY).atDay(5),       "어린이날", HolidayType.SOLAR, true,  HolidayCategory.PUBLIC_HOLIDAY),
        Holiday(y.atMonth(Month.JUNE).atDay(6),      "현충일",   HolidayType.SOLAR, true,  HolidayCategory.COMMEMORATION),
        Holiday(y.atMonth(Month.AUGUST).atDay(15),   "광복절",   HolidayType.SOLAR, true,  HolidayCategory.NATIONAL_DAY),
        Holiday(y.atMonth(Month.OCTOBER).atDay(3),   "개천절",   HolidayType.SOLAR, true,  HolidayCategory.NATIONAL_DAY),
        Holiday(y.atMonth(Month.OCTOBER).atDay(9),   "한글날",   HolidayType.SOLAR, true,  HolidayCategory.NATIONAL_DAY),
        Holiday(y.atMonth(Month.DECEMBER).atDay(25), "성탄절",   HolidayType.SOLAR, true,  HolidayCategory.PUBLIC_HOLIDAY),
    )
}

/** 국경일이지만 공휴일은 아닌 날 (예: 제헌절) */
private fun nationalDaysNonPublic(year: Int): List<Holiday> {
    val y = Year.of(year)
    return listOf(
        Holiday(y.atMonth(Month.JULY).atDay(17), "제헌절", HolidayType.SOLAR, false, HolidayCategory.NATIONAL_DAY)
    )
}

/** 널리 쓰는 국가 기념일(빨간날 아님) — 필요 시 계속 추가 */
private fun commemorations(year: Int): List<Holiday> {
    val y = Year.of(year)
    fun nthWeekdayOfMonth(year: Int, month: Month, day: DayOfWeek, n: Int): LocalDate {
        val first = LocalDate.of(year, month, 1)
        val diff = ((day.value - first.dayOfWeek.value + 7) % 7)
        return first.plusDays(diff.toLong() + (n - 1) * 7L)
    }
    return listOf(
        Holiday(y.atMonth(Month.APRIL).atDay(5), "식목일", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
        Holiday(y.atMonth(Month.MAY).atDay(1),   "근로자의날", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
        Holiday(y.atMonth(Month.MAY).atDay(8),   "어버이날", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
        Holiday(y.atMonth(Month.MAY).atDay(15),  "스승의날", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
        Holiday(nthWeekdayOfMonth(year, Month.MAY, DayOfWeek.MONDAY, 3), "성년의날", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
        Holiday(y.atMonth(Month.OCTOBER).atDay(1), "국군의날", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
        Holiday(y.atMonth(Month.NOVEMBER).atDay(9),"소방의날", HolidayType.SOLAR, false, HolidayCategory.COMMEMORATION),
    )
}

/* ----------------------- 대체공휴일(간단 규칙) ----------------------- */

private fun applySubstitutes(holidays: MutableMap<LocalDate, Holiday>) {
    val toAdd = mutableListOf<Pair<LocalDate, Holiday>>()
    holidays.values.forEach { h ->
        if (h.isPublic && h.date.dayOfWeek == DayOfWeek.SUNDAY) {
            var cand = h.date.plusDays(1)
            while (holidays.containsKey(cand)) cand = cand.plusDays(1)
            toAdd += cand to Holiday(cand, "${h.name} 대체공휴일", HolidayType.SOLAR, true, HolidayCategory.PUBLIC_HOLIDAY)
        }
    }
    toAdd.forEach { (d, hol) -> holidays[d] = hol }
}

/* ----------------------- 외부 API ----------------------- */

/** 연도 전체 맵 (양력 + 넘겨준 음력 확정일 + 대체공휴일) */
fun getKoreanHolidays(
    year: Int,
    lunarOverrides: List<Holiday> = emptyList(),
    applySubstitute: Boolean = true
): Map<LocalDate, Holiday> {
    val map = mutableMapOf<LocalDate, Holiday>()
    (solarHolidays(year) + nationalDaysNonPublic(year) + commemorations(year)).forEach { map[it.date] = it }
    lunarOverrides.filter { it.date.year == year }.forEach { map[it.date] = it }
    if (applySubstitute) applySubstitutes(map)
    return map.toSortedMap()
}

/** 특정 월만 보고 싶을 때 */
fun getKoreanHolidaysForMonth(
    ym: YearMonth,
    lunarOverrides: List<Holiday> = emptyList(),
    applySubstitute: Boolean = true
): Map<LocalDate, Holiday> =
    getKoreanHolidays(ym.year, lunarOverrides, applySubstitute)
        .filterKeys { it.year == ym.year && it.month == ym.month }
