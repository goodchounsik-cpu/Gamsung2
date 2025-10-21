package com.gamsung2.data

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class HolidayIcsRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build(),
    // 구글 공개 캘린더: 대한민국 공휴일
    private val icsUrl: String =
        "https://calendar.google.com/calendar/ical/ko.south_korea%23holiday%40group.v.calendar.google.com/public/basic.ics"
) {
    private val cache = mutableMapOf<Int, Map<LocalDate, Holiday>>()
    private val dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd")

    suspend fun getHolidays(year: Int): Map<LocalDate, Holiday> {
        cache[year]?.let { return it }

        val req = Request.Builder().url(icsUrl).get().build()
        val resp = client.newCall(req).execute()
        val body = resp.body?.string().orEmpty()
        resp.close()

        val parsed = parseIcs(body).filterKeys { it.year == year }
        cache[year] = parsed
        return parsed
    }

    private fun parseIcs(text: String): Map<LocalDate, Holiday> {
        val unfolded = unfoldLines(text)
        val result = mutableMapOf<LocalDate, Holiday>()

        var inEvent = false
        var date: LocalDate? = null
        var summary: String? = null

        BufferedReader(StringReader(unfolded)).use { br ->
            var line: String?
            while (true) {
                line = br.readLine() ?: break
                when {
                    line.startsWith("BEGIN:VEVENT") -> { inEvent = true; date = null; summary = null }
                    line.startsWith("END:VEVENT") -> {
                        if (inEvent && date != null && !summary.isNullOrBlank()) {
                            val name = summary!!.trim()
                            val cat = classify(name)
                            val isPublic = isPublicHoliday(name)
                            result[date!!] = Holiday(
                                date = date!!,
                                name = name,
                                type = HolidayType.SOLAR, // ICS는 양력 확정일
                                isPublic = isPublic,
                                category = cat
                            )
                        }
                        inEvent = false
                    }
                    inEvent && line.startsWith("DTSTART") -> {
                        // DTSTART;VALUE=DATE:20251003  또는  DTSTART:20251003T000000Z
                        val raw = line.substringAfter(':').trim()
                        val ymd = if (raw.length >= 8) raw.substring(0, 8) else raw
                        runCatching { LocalDate.parse(ymd, dateFmt) }.onSuccess { date = it }
                    }
                    inEvent && line.startsWith("SUMMARY") -> {
                        summary = line.substringAfter(':').trim()
                    }
                }
            }
        }
        return result
    }

    /** ICS line folding 해제: 다음 줄이 공백/탭으로 시작하면 이전 줄 이어붙임 */
    private fun unfoldLines(text: String): String {
        val sb = StringBuilder()
        val reader = BufferedReader(StringReader(text))
        var prev: String? = null
        while (true) {
            val line = reader.readLine() ?: break
            if (line.startsWith(" ") || line.startsWith("\t")) {
                prev = (prev ?: "") + line.substring(1)
            } else {
                if (prev != null) sb.appendLine(prev)
                prev = line
            }
        }
        if (prev != null) sb.appendLine(prev)
        return sb.toString()
    }

    private fun isPublicHoliday(name: String): Boolean {
        val reds = listOf(
            "설날", "설날 연휴", "추석", "추석 연휴", "부처님 오신 날",
            "어린이날", "삼일절", "현충일", "광복절", "개천절", "한글날",
            "성탄절", "신정", "대체공휴일"
        )
        return reds.any { name.contains(it) }
    }

    private fun classify(name: String): HolidayCategory {
        val national = listOf("삼일절", "제헌절", "광복절", "개천절", "한글날")
        return when {
            name.contains("대체공휴일") -> HolidayCategory.PUBLIC_HOLIDAY
            national.any { name.contains(it) } -> HolidayCategory.NATIONAL_DAY
            isPublicHoliday(name) -> HolidayCategory.PUBLIC_HOLIDAY
            else -> HolidayCategory.COMMEMORATION
        }
    }
}
