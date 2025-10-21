package com.gamsung2.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

// yyyy-M-d / yyyy-MM-dd 전부 허용
private val FLEX_DATE: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR)   // 1~12 (0 패딩 불필요)
    .appendLiteral('-')
    .appendValue(ChronoField.DAY_OF_MONTH)    // 1~31 (0 패딩 불필요)
    .toFormatter()

/** "2025-10-7" -> LocalDate(2025-10-07) */
fun parseFlex(date: String): LocalDate = LocalDate.parse(date.trim(), FLEX_DATE)

/** 표준 문자열로 정규화: "2025-10-7" -> "2025-10-07" */
fun normalizeYmd(date: String): String = parseFlex(date).toString() // always yyyy-MM-dd
