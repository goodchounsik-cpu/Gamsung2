package com.gamsung2.remote.weather

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 기상청 단기예보 API (getVilageFcst)
 * base_date: yyyymmdd, base_time: HHmm
 * nx, ny: 격자 좌표
 */
interface WeatherApi {

    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst")
    suspend fun getVilageFcst(
        @Query("serviceKey") key: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): VilageFcstResponse
}
