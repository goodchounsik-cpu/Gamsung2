// app/src/main/java/com/gamsung2/data/PlaceRepository.kt
package com.gamsung2.data

import com.gamsung2.model.Place
import com.gamsung2.remote.PlaceApi

class PlaceRepository(
    private val api: PlaceApi
) {

    suspend fun search(
        category: String,            // "lodging" | "restaurant"
        lat: Double?,
        lng: Double?,
        radiusKm: Double,
        typesCsv: String? = null,    // 숙소:   "호텔,모텔,펜션,민박"
        cuisinesCsv: String? = null, // 식당:   "한식,고기/바베큐,카페,해산물,분식/면"
        minRating: Double? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): List<Place> {
        // 실제 API는 시도만 (결과는 현재 더미 사용)
        runCatching {
            api.searchPlaces(
                category = category,
                lat = lat,
                lng = lng,
                radiusKm = radiusKm,
                typesCsv = typesCsv,
                cuisinesCsv = cuisinesCsv,
                minRating = minRating,
                page = page.coerceAtLeast(1),
                pageSize = pageSize.coerceIn(1, 100)
            )
        }

        val isLodging = category == "lodging"
        val all = generateFakePlaces(
            isLodging = isLodging,
            total = 60,
            lat = lat,
            lng = lng
        ).filter { p ->
            val passRating = (minRating == null) || (p.rating == null) || (p.rating >= minRating)
            val passType = when {
                isLodging && !typesCsv.isNullOrBlank() -> {
                    val set = typesCsv.split(',').map { it.trim().lowercase() }.toSet()
                    p.badge?.lowercase() in set
                }
                !isLodging && !cuisinesCsv.isNullOrBlank() -> {
                    val set = cuisinesCsv.split(',').map { it.trim().lowercase() }.toSet()
                    p.badge?.lowercase() in set
                }
                else -> true
            }
            passRating && passType
        }

        val safePage = page.coerceAtLeast(1)
        val safeSize = pageSize.coerceIn(1, 100)
        val from = ((safePage - 1) * safeSize).coerceAtMost(all.size)
        val to = (from + safeSize).coerceAtMost(all.size)
        return all.subList(from, to)
    }

    companion object {
        fun default(placeApi: PlaceApi) = PlaceRepository(placeApi)
    }
}

/* ----------------------- private helpers ----------------------- */

private fun generateFakePlaces(
    isLodging: Boolean,
    total: Int,
    lat: Double?,
    lng: Double?
): List<Place> {
    return List(total) { idx ->
        val numId = idx + 1
        val badgeListLodging = listOf("호텔", "모텔", "펜션", "민박")
        val badgeListFood    = listOf("한식", "고기/바베큐", "카페", "해산물", "분식/면")
        val badge = if (isLodging) badgeListLodging[idx % badgeListLodging.size]
        else badgeListFood[idx % badgeListFood.size]

        val distKm = if (lat != null && lng != null) 0.2 + (idx % 12) * 0.18 else null

        Place(
            id = "fake_$numId", // 🔧 String 으로 변경
            name = if (isLodging) "숙소 #$numId" else "식당 #$numId",
            subtitle = if (isLodging) "깨끗한 숙소" else "맛있는 집",
            badge = badge,
            distanceKm = distKm,
            rating = if (idx % 3 == 0) 4.5 else 4.0
        )
    }
}
