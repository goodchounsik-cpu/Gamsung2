package com.gamsung2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 날짜/시간은 문자열 포맷 고정: date=YYYY-MM-DD, time=HH:mm */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,             // "YYYY-MM-DD"
    val title: String,
    val memo: String? = null,
    val allDay: Boolean = true,
    val startTime: String? = null, // "HH:mm" (allDay면 NULL)
    val endTime: String? = null,   // "HH:mm" (allDay면 NULL)
    val colorArgb: Int? = null
)
