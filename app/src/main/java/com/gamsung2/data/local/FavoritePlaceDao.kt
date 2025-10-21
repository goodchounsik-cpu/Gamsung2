package com.gamsung2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePlaceDao {

    @Query("SELECT * FROM favorite_places ORDER BY id DESC")
    fun getAllFlow(): Flow<List<FavoritePlaceEntity>>

    @Query("SELECT COUNT(*) FROM favorite_places")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT * FROM favorite_places WHERE placeId = :placeId LIMIT 1")
    suspend fun findByPlaceId(placeId: String): FavoritePlaceEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_places WHERE placeId = :placeId)")
    fun isFavoriteFlow(placeId: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(entity: FavoritePlaceEntity): Long

    @Upsert
    suspend fun upsertAll(entities: List<FavoritePlaceEntity>)

    @Delete
    suspend fun delete(entity: FavoritePlaceEntity)

    @Query("DELETE FROM favorite_places WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM favorite_places WHERE placeId = :placeId")
    suspend fun deleteByPlaceId(placeId: String)

    @Query("DELETE FROM favorite_places WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM favorite_places")
    suspend fun clear()
}