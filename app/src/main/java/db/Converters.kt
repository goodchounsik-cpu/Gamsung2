package com.gamsung2.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter fun dateFromString(s: String?): LocalDate? = s?.let(LocalDate::parse)
    @TypeConverter fun dateToString(d: LocalDate?): String? = d?.toString()

    @TypeConverter fun timeFromString(s: String?): LocalTime? = s?.let(LocalTime::parse)
    @TypeConverter fun timeToString(t: LocalTime?): String? = t?.toString()
}
