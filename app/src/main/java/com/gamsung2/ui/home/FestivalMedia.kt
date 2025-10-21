package com.gamsung2.ui.home

data class FestivalMedia(
    val title: String,
    val city: String,
    val videoUrl: String? = null,       // 있으면 자동재생
    val imageUrls: List<String> = emptyList(), // 없으면 이미지 슬라이드
    val deeplink: String? = null
)

// TODO: 나중에 현재 위치(lat,lng)로 서버에서 가져오기
fun demoFestivalForCurrentArea(): FestivalMedia =
    FestivalMedia(
        title = "○○시 가을빛 축제",
        city = "○○시",
        // 테스트: 영상/이미지 중 하나만 채워서 확인해봐
        // videoUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4",
        imageUrls = listOf(
            "https://picsum.photos/seed/festival1/980/540",
            "https://picsum.photos/seed/festival2/980/540",
            "https://picsum.photos/seed/festival3/980/540"
        ),
        deeplink = "app://festival/123"
    )
