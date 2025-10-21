// app/src/main/java/com/gamsung2/data/remote/model/PlaceResponse.kt
package com.gamsung2.model

/** GET /places 응답 루트 */
data class PlaceResponse(
    val items: List<PlaceDto> = emptyList()
)

/** 장소 아이템 */
data class PlaceDto(
    val id: String = "",
    val name: String = "",
    val desc: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val rating: Double? = null,
    val price: Int? = null,
    val distanceMeters: Double? = null
)
