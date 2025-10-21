package com.gamsung2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 여행 그룹(가족, 친구, 단체 등) 테이블
 */
@Entity
data class TravelGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,     // 자동 증가 ID
    val name: String      // 그룹 이름 (예: "가족", "친구")
)

/**
 * 그룹 멤버(전화번호) 테이블
 * - 하나의 그룹에 여러 명이 속할 수 있음
 * - groupId + phone이 Primary Key
 */
@Entity(primaryKeys = ["groupId", "phone"])
data class GroupMember(
    val groupId: Long,    // TravelGroup.id
    val phone: String     // 전화번호
)
