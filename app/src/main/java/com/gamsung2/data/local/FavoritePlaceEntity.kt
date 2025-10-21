// app/src/main/java/com/gamsung2/data/local/FavoritePlaceEntity.kt
package com.gamsung2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

/** 즐겨찾기 장소 엔티티 */
@Entity(tableName = "favorite_places")
data class FavoritePlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val placeId: String,                 // ← 고유 식별자(중복 방지용)
    val name: String,
    val lat: Double,
    val lng: Double,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toLatLng(): LatLng = LatLng(lat, lng)

    companion object {
        fun fromLatLng(
            placeId: String,
            latLng: LatLng,
            name: String,
            note: String? = null
        ) = FavoritePlaceEntity(
            placeId = placeId,
            name = name,
            lat = latLng.latitude,
            lng = latLng.longitude,
            note = note
        )
    }
}
