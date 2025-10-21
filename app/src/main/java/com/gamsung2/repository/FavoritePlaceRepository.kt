// app/src/main/java/com/gamsung2/repository/FavoritePlaceRepository.kt
package com.gamsung2.repository

import com.gamsung2.data.local.FavoritePlaceDao
import com.gamsung2.data.local.FavoritePlaceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Locale

/**
 * 즐겨찾기 장소 Repository
 * - DAO 얇은 래퍼 + CSV 교체(replaceAll) 유틸
 */
class FavoritePlaceRepository(
    private val dao: FavoritePlaceDao
) {

    /** 전체 즐겨찾기 스트림 */
    val favoritesFlow: Flow<List<FavoritePlaceEntity>> = dao.getAllFlow()

    /** placeId 규칙(이름 + 좌표를 고정 소수점으로) */
    private fun buildPlaceId(name: String, lat: Double, lng: Double): String {
        val nm = name.trim()
        val latS = String.format(Locale.US, "%.5f", lat)
        val lngS = String.format(Locale.US, "%.5f", lng)
        return "$nm@$latS,$lngS"
    }

    /* ---------- Read ---------- */

    /** 해당 placeId가 즐겨찾기인지 여부 스트림 */
    fun isFavoriteFlow(placeId: String): Flow<Boolean> = dao.isFavoriteFlow(placeId)

    /** 단발성 확인이 필요할 때 */
    suspend fun isFavorite(placeId: String): Boolean = isFavoriteFlow(placeId).first()

    /* ---------- Write ---------- */

    /** 단건 upsert */
    suspend fun upsert(entity: FavoritePlaceEntity): Long = dao.upsert(entity)

    /** 여러 건 upsert */
    suspend fun upsertAll(entities: List<FavoritePlaceEntity>) = dao.upsertAll(entities)

    /** id 로 삭제 */
    suspend fun deleteById(id: Long) = dao.deleteById(id)

    /** placeId 로 삭제 */
    suspend fun deleteByPlaceId(placeId: String) = dao.deleteByPlaceId(placeId)

    /** 전체 삭제 */
    suspend fun clear() = dao.clear()

    /**
     * CSV/Import 등 새 리스트로 **완전 교체**.
     * - 기존 데이터 삭제 후 새 데이터 id=0 으로 upsert
     */
    suspend fun replaceAll(newList: List<FavoritePlaceEntity>) {
        dao.clear()
        dao.upsertAll(newList.map { it.copy(id = 0) })
    }

    /** 이름/메모/좌표로 단건 추가 */
    suspend fun addFavorite(
        name: String,
        note: String?,
        lat: Double,
        lng: Double
    ): Long = upsert(
        FavoritePlaceEntity(
            id = 0,
            placeId = buildPlaceId(name, lat, lng),   // placeId 반드시 채움
            name = name,
            note = note,
            lat = lat,
            lng = lng,
            createdAt = System.currentTimeMillis()
        )
    )
}
