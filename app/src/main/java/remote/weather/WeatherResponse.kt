package com.gamsung2.remote.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VilageFcstResponse(
    @Json(name = "response") val response: BodyWrapper?
)

@JsonClass(generateAdapter = true)
data class BodyWrapper(
    @Json(name = "body") val body: Body?
)

@JsonClass(generateAdapter = true)
data class Body(
    @Json(name = "items") val items: ItemsWrapper?,
    @Json(name = "totalCount") val totalCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class ItemsWrapper(
    @Json(name = "item") val item: List<FcstItem> = emptyList()
)

/** 필요할 때 꺼내 쓸 최소 필드만 */
@JsonClass(generateAdapter = true)
data class FcstItem(
    @Json(name = "category") val category: String? = null,
    @Json(name = "fcstValue") val fcstValue: String? = null,
    @Json(name = "fcstTime") val fcstTime: String? = null,
    @Json(name = "fcstDate") val fcstDate: String? = null
)
