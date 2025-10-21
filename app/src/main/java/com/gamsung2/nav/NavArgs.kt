package com.gamsung2.nav

import androidx.lifecycle.SavedStateHandle

data class PlaceDetailArgs(
    val placeId: String,
    val title: String,
    val companion: String,
    val lat: Double?,
    val lon: Double?
) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle): PlaceDetailArgs =
            PlaceDetailArgs(
                placeId   = savedStateHandle.get<String>(Routes.Args.PLACE_ID) ?: "",
                title     = savedStateHandle.get<String>(Routes.Args.TITLE).orEmpty(),
                companion = savedStateHandle.get<String>(Routes.Args.COMPANION).orEmpty(),
                lat       = savedStateHandle.get<String>(Routes.Args.LAT)?.toDoubleOrNull(),
                lon       = savedStateHandle.get<String>(Routes.Args.LON)?.toDoubleOrNull()
            )
    }
}
