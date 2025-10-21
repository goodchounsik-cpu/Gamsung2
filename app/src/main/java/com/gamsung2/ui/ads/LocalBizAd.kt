package com.gamsung2.data.ads

data class LocalBizAd(
    val id: String,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val bizName: String,
    val address: String?,
    val deepLink: String?,   // "app://place/{id}" 또는 외부 URL
    val lat: Double?,        // 위치 타게팅용(옵션)
    val lon: Double?,
    val startAt: Long?,      // 노출기간(옵션)
    val endAt: Long?
)
