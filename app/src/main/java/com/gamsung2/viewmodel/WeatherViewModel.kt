package com.gamsung2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.local.DailyWeather
import com.gamsung2.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate

class WeatherViewModel(
    private val repo: WeatherRepository
) : ViewModel() {

    /** 무료/유료 모드 flag (true = 서버, false = 로컬) */
    var premiumMode: Boolean = false

    /** 기간 조회 */
    fun getBetween(start: LocalDate, end: LocalDate): Flow<List<DailyWeather>> =
        repo.getBetween(start, end)
            .catch { emit(emptyList()) } // ✅ 예외시 빈 리스트로 복구(앱 종료 방지)

    /** 저장 */
    fun saveWeather(
        date: LocalDate,
        forecast: String?,
        actual: String?,
        lat: Double? = null,
        lon: Double? = null
    ) {
        val entity = DailyWeather(
            dateEpoch = date.toEpochDay(),
            forecast = forecast,
            actualWeather = actual,
            lat = lat,
            lon = lon
        )
        viewModelScope.launch {
            if (premiumMode) repo.saveServer(entity) else repo.saveLocal(entity)
        }
    }

    /** 지난 데이터 정리 (기본: 1년 이전 삭제) */
    fun cleanOld(days: Long = 365) {
        viewModelScope.launch {
            val cutoff = LocalDate.now().minusDays(days)
            repo.deleteOlderThan(cutoff)
        }
    }
}
