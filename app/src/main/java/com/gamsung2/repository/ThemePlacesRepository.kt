package com.gamsung2.repository

data class ThemePlace(
    val name: String,
    val region: String,
    val summary: String,
    val lat: Double,
    val lng: Double
)

/**
 * 나중에 실제 API/DB로 교체하기 쉬운 형태의 간단 리포지토리.
 * 현재는 theme + companion 조합으로 더미 데이터를 반환한다.
 */
object ThemePlacesRepository {

    private val base = mapOf(
        // 1차 분류: 역사여행 / 힐링여행 / 사진여행 / 액티비티여행 / 자전거여행 / 축제/행사
        "역사여행" to listOf(
            ThemePlace("경복궁", "서울", "조선의 법궁, 야간개장 인기", 37.579617, 126.977041),
            ThemePlace("불국사", "경주", "유네스코 세계문화유산 사찰", 35.790055, 129.331647),
        ),
        "힐링여행" to listOf(
            ThemePlace("아침고요수목원", "가평", "사계절 정원 산책", 37.7436, 127.3510),
            ThemePlace("제주 사려니숲길", "제주", "삼나무/비자림 치유 숲길", 33.4155, 126.7035),
        ),
        "사진여행" to listOf(
            ThemePlace("감천문화마을", "부산", "형형색색 마을 전경", 35.0975, 129.0105),
            ThemePlace("보성녹차밭", "보성", "초록 물결의 대자연", 34.7180, 127.0805),
        ),
        "액티비티여행" to listOf(
            ThemePlace("단양 패러글라이딩", "단양", "남한강 절경을 하늘에서", 36.9919, 128.3658),
            ThemePlace("여수 해상케이블카", "여수", "바다 위 스릴과 야경", 34.7402, 127.7489),
        ),
        "자전거여행" to listOf(
            ThemePlace("한강 자전거길", "서울", "도심 속 라이딩 성지", 37.5286, 126.9326),
            ThemePlace("섬진강 자전거길", "전남/전북", "강변 풍광과 봄벚꽃", 35.1019, 127.7464),
        ),
        "축제/행사" to listOf(
            ThemePlace("진해군항제", "창원", "벚꽃 명소, 군악의장 페스티벌", 35.1547, 128.6596),
            ThemePlace("부평풍물대축제", "인천", "거리예술/퍼레이드", 37.4894, 126.7244),
        ),
    )

    /**
     * 동반자(가족/연인/친구/단체/혼자)에 따라 간단 가중 필터(우선순위)만 적용.
     * 지금은 샘플 스코어로 섞어주고, 실제 도입 시 태그 기반 스코어링으로 교체 가능.
     */
    fun fetch(theme: String, companion: String): List<ThemePlace> {
        val pool = base[theme].orEmpty()
        if (pool.isEmpty()) return emptyList()
        // 간단 섞기: 동반자에 따라 보여주는 순서만 조금 바꾼다
        val bias = when (companion) {
            "가족" -> 0
            "연인" -> 1
            "친구" -> 2
            "단체" -> 3
            else   -> 4 // 혼자
        }
        return pool.sortedBy { it.name.hashCode() xor bias }
    }
}
