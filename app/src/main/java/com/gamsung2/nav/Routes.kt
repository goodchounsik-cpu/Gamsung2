package com.gamsung2.nav

import android.net.Uri

object Routes {
    // 홈
    const val HOME_NEAR = "home_near"
    const val HOME_ALL  = "home_all"
    const val HOME      = "home"

    // Auth
    const val AUTH_GATE      = "auth_gate"
    const val LOGIN          = "login"
    const val SIGNUP_CHOICE  = "signup_choice"
    const val SIGNUP_GENERAL = "signup_general"
    const val SIGNUP_BIZ     = "signup_biz"
    const val SIGNUP_GOV     = "signup_gov"

    // 기본 화면
    const val MAP       = "map"
    const val FAVORITES = "favorites"
    const val TRANSLATE = "translate"

    // 달력
    const val MONTH_CAL      = "monthCal"
    const val CALENDAR_MONTH = MONTH_CAL

    // 테마
    const val THEME_TRAVEL  = "themeTravel"
    const val THEME_DETAIL  = "themeDetail"
    const val THEME_GALLERY = "themeGallery"

    // 추천 코스 전체
    const val RECO_FULL = "reco_full"

    // 카테고리별 장소 목록
    const val CATEGORY_PLACE_LIST = "category_place_list"

    // 장소 상세
    const val PLACE_DETAIL = "placeDetail"

    // 일정 편집
    const val EVENT_EDITOR = "event_editor"

    // 검색/플래너
    const val SEARCH  = "search"
    const val PLANNER = "com/gamsung2/planner"

    // 코스 대화형 플래너
    const val COURSE_CHAT = "course_chat"

    // 내 정보 편집
    const val EDIT_PROFILE = "edit_profile"

    // 나만의 여행 / 버킷
    const val MY_TRIP     = "my_trip"
    const val BUCKET_HOME = "bucket_home"

    object Args {
        const val TITLE     = "title"
        const val KEY       = "key"
        const val PLACE_ID  = "placeId"
        const val COMPANION = "companion"
        const val LAT       = "lat"
        const val LON       = "lon"
        const val LNG       = "lng"
        const val LABEL     = "label"
        const val DATE      = "date"
        const val EVENT_ID  = "eventId"

        // 검색/플래너 공통
        const val QUERY    = "query"
        const val REGION   = "region"
        const val CATEGORY = "category"
        const val SEED     = "seed"
        const val SELECTED = "selected"
        const val DAYS     = "days"

        // 기타
        const val GROUP     = "group"
        const val WIDE      = "wide"
        const val RADIUS_KM = "radiusKm"

        // ✅ 버킷 종류
        const val BUCKET = "bucket" // "", "course", "food", "lodging"
    }

    /** 장소 상세 (✅ bucket 쿼리 지원) */
    fun placeDetailRoute(
        placeId: String,
        companion: String = "",
        title: String = "",
        lat: Double? = null,
        lon: Double? = null,
        bucket: String? = null
    ): String = build(
        "$PLACE_DETAIL/${Uri.encode(placeId)}",
        mapOf(
            Args.COMPANION to companion,
            Args.TITLE to title,
            Args.LAT to (lat?.toString().orEmpty()),
            Args.LON to (lon?.toString().orEmpty()),
            Args.BUCKET to (bucket ?: "")
        )
    )

    /** 지도 */
    fun mapRoute(lat: Double? = null, lon: Double? = null, label: String? = null): String =
        build(
            MAP,
            mapOf(
                Args.LAT to (lat?.toString().orEmpty()),
                Args.LON to (lon?.toString().orEmpty()),
                Args.LABEL to (label ?: "")
            )
        )

    /** 월 달력 */
    fun calendarMonthRoute(): String = CALENDAR_MONTH

    /** 일정 편집 */
    fun eventEditorRoute(date: String, eventId: Long? = null): String =
        build(
            EVENT_EDITOR,
            mapOf(
                Args.DATE to date,
                Args.EVENT_ID to (eventId?.toString() ?: "-1")
            )
        )

    /** 카테고리별 장소 목록 */
    fun categoryPlaceListRoute(
        category: String,
        title: String = "목록",
        themeTitle: String = "",
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double = 3.0,
        wide: Boolean = false
    ): String = build(
        CATEGORY_PLACE_LIST,
        mapOf(
            "category" to category,
            "title" to title,
            "themeTitle" to themeTitle,
            Args.LAT to (lat?.toString().orEmpty()),
            Args.LNG to (lng?.toString().orEmpty()),
            Args.RADIUS_KM to radiusKm.toString(),
            Args.WIDE to if (wide) "1" else "0"
        )
    )

    /** 검색 (bucket 포함) */
    fun searchRoute(
        query: String = "",
        bucket: String? = null,
        region: String? = null,
        category: String? = null
    ): String = build(
        SEARCH,
        mapOf(
            Args.QUERY to query,
            Args.BUCKET to (bucket ?: ""),
            Args.REGION to (region ?: ""),
            Args.CATEGORY to (category ?: "")
        )
    )

    /** 플래너 */
    fun plannerRoute(
        seedQuery: String? = null,
        selectedPlaceIds: List<String> = emptyList(),
        days: Int? = null
    ): String = build(
        PLANNER,
        mapOf(
            Args.SEED to (seedQuery ?: ""),
            Args.SELECTED to selectedPlaceIds.joinToString(","),
            Args.DAYS to (days?.toString() ?: "")
        )
    )

    /** 추천 코스 전체 */
    fun recoFullRoute(key: String, group: String, region: String): String =
        build(
            RECO_FULL,
            mapOf(
                Args.KEY to key,
                Args.GROUP to group,
                Args.REGION to region
            )
        )

    fun myTripRoute(): String = MY_TRIP
    fun bucketHomeRoute(): String = BUCKET_HOME

    /** 공통 쿼리 빌더 */
    private fun build(base: String, params: Map<String, String>): String {
        val q = params.filter { it.value.isNotBlank() }
            .map { (k, v) -> "$k=${Uri.encode(v)}" }
        return if (q.isEmpty()) base else "$base?${q.joinToString("&")}"
    }
}
