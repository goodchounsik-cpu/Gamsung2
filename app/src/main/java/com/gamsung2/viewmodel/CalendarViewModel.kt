// app/src/main/java/com/gamsung2/viewmodel/CalendarViewModel.kt
package com.gamsung2.viewmodel

import androidx.lifecycle.ViewModel
import com.gamsung2.data.Holiday
import com.gamsung2.util.getKoreanHolidays
import java.time.LocalDate

class CalendarViewModel : ViewModel() {
    private val cache = mutableMapOf<Int, List<Holiday>>() // year -> holidays

    private fun ofYear(year: Int): List<Holiday> =
        cache.getOrPut(year) { getKoreanHolidays(year) }

    fun holidayOn(date: LocalDate): Holiday? =
        ofYear(date.year).firstOrNull { it.date == date }

    fun isHoliday(date: LocalDate): Boolean = holidayOn(date) != null
    fun holidayName(date: LocalDate): String? = holidayOn(date)?.name
}
