package com.gamsung2.data.model

/** 장소 DTO – 네트워크/DB 공용으로 쓰기 쉬운 최소 필드셋 */
data class PlaceDto(
    val id: String,
    val name: String,
    val desc: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val rating: Double? = null,
    val price: Int? = null,
    val distanceMeters: Double? = null
)
