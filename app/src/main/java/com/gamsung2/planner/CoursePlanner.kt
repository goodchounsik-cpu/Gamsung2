package com.gamsung2.planner

import kotlin.math.*

/** 사용자가 말한 요구를 파싱한 결과 */
data class ParsedQuery(
    val needSea: Boolean,
    val needHike: Boolean,
    val maxDriveMin: Int,      // 허용 이동 시간(분)
    val startLat: Double,
    val startLon: Double
)

/** 코스의 한 스탑(정류장) */
data class CourseStop(
    val id: String,
    val title: String,
    val lat: Double,
    val lon: Double,
    val kind: Kind
) {
    enum class Kind { SEA, HIKE, ETC }
}

/** 추천된 2-스탑 코스 */
data class CoursePlan(
    val items: List<CourseStop>,   // 보통 [바다, 등산] 순서
    val totalDriveMin: Int
)

/* ---------------------------------------------
 * 1) 간단 파서: 자연어에서 키워드/시간을 추출
 * --------------------------------------------- */
fun parseFreeText(text: String, start: Pair<Double, Double>): ParsedQuery {
    val t = text.lowercase()

    val needSea  = listOf("바다","해변","해수욕장","바닷","sea","ocean").any { t.contains(it) }
    val needHike = listOf("등산","산","트레킹","hike","산책로","봉우리").any { t.contains(it) }

    // 시간 숫자 뽑기: "1시간", "90분", "1.5시간", "한시간" 등 대충 처리
    val hourRegex = Regex("""(\d+(?:\.\d+)?)\s*시간""")
    val minRegex  = Regex("""(\d+)\s*분""")

    val hour = hourRegex.find(t)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
    val min  = minRegex.find(t)?.groupValues?.getOrNull(1)?.toIntOrNull()

    val maxDriveMin = when {
        hour != null -> (hour * 60).roundToInt()
        min  != null -> min
        else -> 60 // 기본 1시간
    }.coerceIn(20, 180)

    return ParsedQuery(
        needSea = needSea,
        needHike = needHike,
        maxDriveMin = maxDriveMin,
        startLat = start.first,
        startLon = start.second
    )
}

/* ---------------------------------------------
 * 2) 간단 후보지(샘플 데이터, 서울권)
 *    실제 서비스에선 검색/DB 연동
 * --------------------------------------------- */
private val SEA_CANDIDATES = listOf(
    CourseStop("sea_eurwang","을왕리해수욕장", 37.4473,126.3729, CourseStop.Kind.SEA),
    CourseStop("sea_sorae","소래포구",         37.4009,126.7335, CourseStop.Kind.SEA),
    CourseStop("sea_songdo","송도달빛축제공원",37.3880,126.6373, CourseStop.Kind.SEA)
)

private val HIKE_CANDIDATES = listOf(
    CourseStop("hike_inwang","인왕산",   37.5793,126.9574, CourseStop.Kind.HIKE),
    CourseStop("hike_bukhan","북한산",   37.6581,126.9770, CourseStop.Kind.HIKE),
    CourseStop("hike_achasan","아차산",  37.5626,127.1039, CourseStop.Kind.HIKE)
)

/* ---------------------------------------------
 * 3) 거리/시간 유틸
 * --------------------------------------------- */
private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat/2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1-a))
    return R * c
}

/** 아주 러프한 주행시간(분): 평균 40km/h 가정 */
private fun estimateDriveMinKm(distanceKm: Double): Int =
    ((distanceKm / 40.0) * 60.0).roundToInt().coerceAtLeast(5)

/* ---------------------------------------------
 * 4) 코스 빌더: (바다→등산) 페어링해서 시간 제한 내 추천
 * --------------------------------------------- */
fun buildPlans(q: ParsedQuery, maxResults: Int = 5): List<CoursePlan> {
    // 요구조건이 없으면 일단 바다+등산 둘 다로 가정
    val needSea  = if (!q.needSea && !q.needHike) true else q.needSea
    val needHike = if (!q.needSea && !q.needHike) true else q.needHike

    val sea = if (needSea) SEA_CANDIDATES else emptyList()
    val hik = if (needHike) HIKE_CANDIDATES else emptyList()

    // 모든 페어링 조합
    val pairs = mutableListOf<CoursePlan>()
    for (s in sea) for (h in hik) {
        // 출발지→s + s→h 단순 합산(대략)
        val d1 = haversineKm(q.startLat, q.startLon, s.lat, s.lon)
        val d2 = haversineKm(s.lat, s.lon, h.lat, h.lon)
        val totalMin = estimateDriveMinKm(d1 + d2)

        if (totalMin <= q.maxDriveMin) {
            pairs += CoursePlan(items = listOf(s, h), totalDriveMin = totalMin)
        }
    }

    // 가까운 순으로 상위 반환(없으면 제한 넘어도 상위 2~3개라도 제시)
    val sorted = pairs.sortedBy { it.totalDriveMin }
    if (sorted.isNotEmpty()) return sorted.take(maxResults)

    // 제한내가 없다면, 가장 가까운 것들 fallback
    val fallback = mutableListOf<CoursePlan>()
    for (s in sea) for (h in hik) {
        val totalMin = estimateDriveMinKm(
            haversineKm(q.startLat, q.startLon, s.lat, s.lon) +
                    haversineKm(s.lat, s.lon, h.lat, h.lon)
        )
        fallback += CoursePlan(listOf(s, h), totalMin)
    }
    return fallback.sortedBy { it.totalDriveMin }.take(3)
}
