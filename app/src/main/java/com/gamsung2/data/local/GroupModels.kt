package com.gamsung2.data.local

/** Firestore에 저장되는 멤버 정보 */
data class Member(
    val uid: String = "",
    val name: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val lastTs: Long? = null,
    val battery: Int? = null
)

/** Firestore에 저장되는 그룹 메타 정보 */
data class GroupMeta(
    val code: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
