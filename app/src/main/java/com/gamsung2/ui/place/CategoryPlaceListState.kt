package com.gamsung2.ui.place

import com.gamsung2.model.Place

// ✅ 어디서나 쓸 수 있도록 톱레벨 enum
enum class LodgingType(val label: String) { HOTEL("호텔"), MOTEL("모텔"), PENSION("펜션"), MINBAK("민박") }
enum class CuisineType(val label: String) { KOREAN("한식"), BBQ("고기/바베큐"), CAFE("카페"), SEAFOOD("해산물"), NOODLES("분식/면") }

// ✅ 화면 상태
data class CategoryPlaceUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<Place> = emptyList(),

    val radiusKm: Double = 1.0,
    val minRating: Double = 0.0,
    val lodgingTypes: Set<LodgingType> = emptySet(),
    val cuisines: Set<CuisineType> = emptySet(),

    val page: Int = 1,
    val hasMore: Boolean = true
)
