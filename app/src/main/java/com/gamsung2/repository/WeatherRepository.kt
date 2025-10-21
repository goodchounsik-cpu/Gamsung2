// app/src/main/java/com/gamsung2/repository/WeatherRepository.kt
package com.gamsung2.repository

import com.gamsung2.data.local.DailyWeather
import com.gamsung2.data.local.DailyWeatherDao
import com.gamsung2.remote.weather.WeatherApi      // ✅ 올바른 패키지
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class WeatherRepository(
    private val dao: DailyWeatherDao,
    private val api: WeatherApi
) {
    /** 로컬 저장 */
    suspend fun saveLocal(weather: DailyWeather) = withContext(Dispatchers.IO) {
        dao.upsert(weather)
    }

    /** 서버 저장 (필요 시 구현) */
    suspend fun saveServer(weather: DailyWeather) = withContext(Dispatchers.IO) {
        // TODO: 서버 저장 로직
    }

    /** 예보 조회 후 저장 (기상청 getVilageFcst) */
    suspend fun fetchAndSaveForecast(
        apiKey: String,
        baseDate: LocalDate,
        baseTime: String,
        nx: Int,
        ny: Int,
        lat: Double,
        lon: Double
    ) = withContext(Dispatchers.IO) {
        val dateStr = baseDate.toString().replace("-", "")

        // ✅ WeatherApi 시그니처에 맞춰 호출 (기상청명칭: getVilageFcst)
        val resp = api.getVilageFcst(
            key = apiKey,
            baseDate = dateStr,
            baseTime = baseTime,
            nx = nx,
            ny = ny
        )

        // TODO: resp 파싱해서 forecastString 생성
        val forecastString = "맑음"

        dao.upsert(
            DailyWeather(
                dateEpoch = baseDate.toEpochDay(),
                forecast = forecastString,
                actualWeather = null,
                lat = lat,
                lon = lon
            )
        )
    }

    /** 기간 조회 (epochDay 기반) */
    fun getBetween(start: LocalDate, end: LocalDate): Flow<List<DailyWeather>> =
        dao.getBetween(start.toEpochDay(), end.toEpochDay())

    /** 기준일 이전 데이터 삭제 (epochDay 기반) */
    suspend fun deleteOlderThan(cutoff: LocalDate) = withContext(Dispatchers.IO) {
        // 프로젝트 DAO에 맞춰 함수명을 사용하세요.
        dao.deleteOlderThanEpochDay(cutoff.toEpochDay())
    }
}
