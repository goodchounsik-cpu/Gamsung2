package com.gamsung2.data

import android.content.Context
import com.gamsung2.model.Place
import com.gamsung2.model.PlaceCandidate
import com.gamsung2.model.SearchSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val placeRepo: PlaceRepository,
    @ApplicationContext private val context: Context
) {
    /**
     * 통합 검색: 사용자 키워드(q) + 촬영지 + (옵션) 추천
     * 지금은 PlaceRepository.search 를 재사용하고,
     * assets/filming_index.json (선택) 을 읽어 촬영지 매칭만 추가.
     */
    suspend fun searchAll(q: String, lat: Double?, lon: Double?): List<PlaceCandidate> {
        val base = placeRepo.search(
            category = "restaurant",
            lat = lat, lng = lon,
            radiusKm = 3.0,
            typesCsv = null, cuisinesCsv = q.lowercase(),
            minRating = null, page = 1, pageSize = 20
        ).map { PlaceCandidate(it, SearchSource.USER_QUERY, score = it.rating ?: 0.0) }

        val filming = loadFilmingMatches(q).map {
            PlaceCandidate(it, SearchSource.FILMING, score = 4.5)
        }

        // 간단 중복 제거 (id 기준)
        return (base + filming).distinctBy { it.place.id }
            .sortedByDescending { it.score }
    }

    // 샘플: assets/filming_index.json -> List<Place>
    private fun loadFilmingMatches(q: String): List<Place> {
        // MVP: 파일이 없어도 빈 목록
        return emptyList()
    }
}
