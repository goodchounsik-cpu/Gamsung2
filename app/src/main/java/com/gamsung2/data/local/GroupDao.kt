package com.gamsung2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 여행 그룹 + 그룹 멤버 DAO
 */
@Dao
interface GroupDao {

    /** 모든 그룹 조회 */
    @Query("SELECT * FROM TravelGroup")
    fun getAllGroups(): Flow<List<TravelGroup>>

    /** 그룹 추가/수정 (같은 이름이면 교체) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(group: TravelGroup): Long

    /** 그룹 삭제 */
    @Delete
    suspend fun deleteGroup(group: TravelGroup)

    /** 그룹에 멤버 추가 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMember(member: GroupMember)

    /** 그룹에서 특정 멤버 삭제 */
    @Query("DELETE FROM GroupMember WHERE groupId = :groupId AND phone = :phone")
    suspend fun deleteMember(groupId: Long, phone: String)

    /** 특정 그룹의 멤버 목록 가져오기 */
    @Query("SELECT * FROM GroupMember WHERE groupId = :groupId")
    fun getMembers(groupId: Long): Flow<List<GroupMember>>

    /** 특정 그룹의 모든 멤버 일괄 삭제 (그룹 해제 전용) */
    @Query("DELETE FROM GroupMember WHERE groupId = :groupId")
    suspend fun deleteMembersByGroup(groupId: Long)
}
