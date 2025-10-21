package com.gamsung2.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 실제 서버 붙이기 전까지 httpbin /anything 으로 에코만 받습니다.
 * 나중에 서버 연결 시 @GET 경로와 파라미터, 응답 DTO만 교체하면 됩니다.
 */
interface PlaceApi {

    @GET("anything") // TODO: 실제 서버 경로로 교체
    suspend fun searchPlaces(
        @Query("category") category: String,      // "lodging" | "restaurant"
        @Query("lat")       lat: Double?,
        @Query("lng")       lng: Double?,
        @Query("radiusKm")  radiusKm: Double,
        @Query("types")     typesCsv: String?,    // 숙소 타입 CSV
        @Query("cuisines")  cuisinesCsv: String?, // 음식 카테고리 CSV
        @Query("minRating") minRating: Double?,
        @Query("page")      page: Int,
        @Query("pageSize")  pageSize: Int
    ): HttpBinEchoResponse
}

/** httpbin /anything 최소 응답 타입 (우리는 본문을 쓰지 않음, 타입 맞춤용) */
data class HttpBinEchoResponse(
    val url: String? = null
)
