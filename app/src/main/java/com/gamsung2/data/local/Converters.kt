package com.gamsung2.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String? =
        list?.joinToString("|")?.ifEmpty { null }

    @TypeConverter
    fun toStringList(raw: String?): List<String>? =
        raw?.takeIf { it.isNotEmpty() }?.split("|")
}
