package com.gamsung2.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,                 // 해당 ‘하루’의 일정
    val title: String,
    val note: String? = null,
    val allDay: Boolean = false,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val colorArgb: Int? = null           // 셀 점/배지 색 선택용(옵션)
)
