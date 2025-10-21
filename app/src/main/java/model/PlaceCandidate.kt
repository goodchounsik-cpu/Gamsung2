package com.gamsung2.model
import com.gamsung2.model.Place

data class PlaceCandidate(
    val place: Place,
    val source: SearchSource,
    val score: Double = 0.0,
    val pinned: Boolean = false
)
