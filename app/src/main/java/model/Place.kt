package com.gamsung2.model

/** 장소 공용 도메인 모델 */
data class Place(
    val id: String,
    val name: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val distanceKm: Double? = null,
    val rating: Double? = null
)
