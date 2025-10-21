package com.gamsung2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 날짜별 날씨 저장 엔티티.
 * LocalDate는 Room이 직접 저장 못하므로 epochDay(Long)로 변환하여 사용.
 */
@Entity(tableName = "daily_weather")
data class DailyWeather(
    @PrimaryKey val dateEpoch: Long,           // LocalDate.toEpochDay()
    val forecast: String?,                     // 예보
    val actualWeather: String?,                // 실제 기록된 날씨
    val lat: Double? = null,                   // 위치 위도
    val lon: Double? = null,                   // 위치 경도
    val createdAt: Long = System.currentTimeMillis() // 저장 시각
)
