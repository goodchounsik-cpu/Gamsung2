// app/src/main/java/com/gamsung2/ui/home/FestivalAd.kt
package com.gamsung2.ui.home

data class FestivalAd(
    val id: String,
    val city: String,
    val title: String,
    val date: String,
    val place: String,
    val tagline: String,
    val imageUrl: String? = null,
    val deeplink: String? = null
)
