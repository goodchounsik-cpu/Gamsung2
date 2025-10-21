// app/src/main/java/com/gamsung2/util/WeatherModels.kt
package com.gamsung2.util

import java.time.LocalDate

/** Open-Meteo 일간 예보 1칸 */
data class DailyForecast(
    val date: LocalDate,
    val type: WeatherType,
    val tMin: Double,
    val tMax: Double
)

/** 아주 단순화한 날씨 분류 (UI에서 아이콘/이모지 매핑용) */
enum class WeatherType { SUNNY, CLOUDY, RAIN, SNOW, OTHER }

/** 주간 스트립에서 쓰는 가벼운 UI 모델 */
data class WeekDayForecastUi(
    val emoji: String? = null,
    val min: Int? = null,
    val max: Int? = null
)
