package com.gamsung2.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyWeatherDao {

    /** 저장 또는 갱신 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weather: DailyWeather)

    /** epochDay 범위로 조회 */
    @Query("SELECT * FROM daily_weather WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch")
    fun getBetween(startEpoch: Long, endEpoch: Long): Flow<List<DailyWeather>>

    /** createdAt 기준 오래된 데이터 삭제 */
    @Query("DELETE FROM daily_weather WHERE createdAt < :threshold")
    suspend fun deleteOld(threshold: Long)

    /** dateEpoch 기준 오래된 데이터 삭제 (WeatherRepository.deleteOlderThanEpochDay 에서 사용) */
    @Query("DELETE FROM daily_weather WHERE dateEpoch < :cutoffEpochDay")
    suspend fun deleteOlderThanEpochDay(cutoffEpochDay: Long)
}
