// app/src/main/java/com/gamsung2/data/PlaceModels.kt
package com.gamsung2.data

/**
 * 도메인 모델 (UI/비즈 로직에서 사용하는 타입)
 * - 원격 DTO → 도메인 변환은 remote 패키지의 확장함수(ApiPlaceDto.toDomain) 사용
 */
data class Place(
    val id: String,
    val name: String,
    val subtitle: String?,
    val distanceKm: Double?,
    val badge: String?,
    val rating: Double?
)
