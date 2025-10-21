package com.gamsung2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.local.GroupDao
import com.gamsung2.data.local.GroupMember
import com.gamsung2.data.local.TravelGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 여행 그룹/멤버 관리 ViewModel
 */
class GroupViewModel(
    private val groupDao: GroupDao
) : ViewModel() {

    /** 모든 그룹 Flow */
    val groups: Flow<List<TravelGroup>> = groupDao.getAllGroups()

    /** 특정 그룹의 멤버 목록 Flow */
    fun members(groupId: Long): Flow<List<GroupMember>> = groupDao.getMembers(groupId)

    /** 그룹 추가/수정 */
    fun upsertGroup(name: String) {
        viewModelScope.launch {
            val group = TravelGroup(name = name)
            groupDao.upsertGroup(group)
        }
    }

    /** 그룹 삭제 */
    fun deleteGroup(group: TravelGroup) {
        viewModelScope.launch {
            groupDao.deleteGroup(group)
        }
    }

    /** 멤버 추가 */
    fun addMember(groupId: Long, phone: String) {
        viewModelScope.launch {
            val member = GroupMember(groupId = groupId, phone = phone)
            groupDao.addMember(member)
        }
    }

    /** 멤버 삭제 */
    fun deleteMember(groupId: Long, phone: String) {
        viewModelScope.launch {
            groupDao.deleteMember(groupId, phone)
        }
    }

    /** 그룹 해제(멤버 전체 삭제 후 그룹 삭제) */
    fun disbandGroup(group: TravelGroup) {
        viewModelScope.launch {
            groupDao.deleteMembersByGroup(group.id)
            groupDao.deleteGroup(group)
        }
    }
}
