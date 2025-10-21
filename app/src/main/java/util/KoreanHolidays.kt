package com.gamsung2.util

import android.os.Build
import androidx.annotation.RequiresApi
import android.icu.util.ChineseCalendar
import com.gamsung2.data.Holiday
import com.gamsung2.data.HolidayType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Asia/Seoul 기준 */
private val SEOUL = ZoneId.of("Asia/Seoul")

/** 메인: 해당 연도의 한국 공휴일(양력+음력) 리스트 */
fun getKoreanHolidays(year: Int): List<Holiday> {
    val list = mutableListOf<Holiday>()

    fun addSolar(month: Int, day: Int, name: String) {
        list.add(
            Holiday(
                date = LocalDate.of(year, month, day),
                name = name,
                type = HolidayType.SOLAR,
                isPublic = true
            )
        )
    }

    // --- 양력 고정 공휴일 ---
    addSolar(1, 1, "신정")
    addSolar(3, 1, "삼일절")
    addSolar(5, 5, "어린이날")
    addSolar(6, 6, "현충일")
    addSolar(8, 15, "광복절")
    addSolar(10, 3, "개천절")
    addSolar(10, 9, "한글날")
    addSolar(12, 25, "성탄절")

    // --- 음력 기반 공휴일 (API 24+에서 자동 변환) ---
    list += buildLunarHolidays(year)

    // --- (옵션) 대체공휴일 샘플: 일요일과 겹치면 다음날 보상 ---
    // applySubstituteHolidays(list)

    return list
        .distinctBy { it.date to it.name }   // 중복 방지
        .sortedBy { it.date }
}

/** 화면이 Map을 기대할 때 쓰는 어댑터 */
fun getKoreanHolidaysMap(year: Int): Map<LocalDate, String> =
    getKoreanHolidays(year).associate { it.date to it.name }

/** 음력 변환: 설(3일), 추석(3일), 석가탄신일 */
private fun buildLunarHolidays(year: Int): List<Holiday> {
    if (Build.VERSION.SDK_INT < 24) return emptyList() // 하위 버전은 별도 테이블/라이브러리로 처리
    val out = mutableListOf<Holiday>()

    fun add(date: LocalDate, name: String) {
        out.add(Holiday(date = date, name = name, type = HolidayType.LUNAR, isPublic = true))
    }

    // 설날(음 1/1) + 전날/다음날
    lunarToSolar(year, 1, 1)?.let { base ->
        add(base.minusDays(1), "설날 연휴")
        add(base, "설날")
        add(base.plusDays(1), "설날 연휴")
    }

    // 추석(음 8/15) + 전날/다음날
    lunarToSolar(year, 8, 15)?.let { base ->
        add(base.minusDays(1), "추석 연휴")
        add(base, "추석")
        add(base.plusDays(1), "추석 연휴")
    }

    // 석가탄신일(음 4/8)
    lunarToSolar(year, 4, 8)?.let { buddha ->
        add(buddha, "석가탄신일")
    }

    return out
}

/**
 * 음력(중국식/동아시아 음력) → 양력
 * - month: 1..12 (0-based 아님)
 * - day: 1..30
 * - leapMonth: 윤달이면 true
 *
 * 참고: ICU ChineseCalendar를 사용하므로 Android API 24+ 필요
 */
@RequiresApi(24)
private fun lunarToSolar(
    year: Int,
    month: Int,
    day: Int,
    leapMonth: Boolean = false
): LocalDate? {
    val cc = ChineseCalendar()
    // EXTENDED_YEAR = 서기년 + 2637 (ICU 규칙)
    cc.set(ChineseCalendar.EXTENDED_YEAR, year + 2637)
    // MONTH는 0-based (1월=0)
    cc.set(ChineseCalendar.MONTH, month - 1)
    // 윤달 여부
    cc.set(ChineseCalendar.IS_LEAP_MONTH, if (leapMonth) 1 else 0)
    cc.set(ChineseCalendar.DAY_OF_MONTH, day)

    val instant = Instant.ofEpochMilli(cc.timeInMillis)
    return instant.atZone(SEOUL).toLocalDate()
}

/** (옵션) 대체공휴일 예시: 일요일이면 다음 평일을 보상휴일로 추가 */
@Suppress("unused")
private fun applySubstituteHolidays(list: MutableList<Holiday>) {
    // 실제 법 규정은 조금 더 복잡하지만 샘플 규칙만 적용
    val base = list.toList()
    base.forEach { h ->
        if (h.date.dayOfWeek == DayOfWeek.SUNDAY) {
            val alt = h.date.plusDays(1)
            if (base.none { it.date == alt }) {
                list.add(
                    Holiday(
                        date = alt,
                        name = "${h.name} 대체공휴일",
                        type = h.type,
                        isPublic = true
                    )
                )
            }
        }
    }
}
