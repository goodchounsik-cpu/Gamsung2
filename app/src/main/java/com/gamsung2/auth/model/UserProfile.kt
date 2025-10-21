package com.gamsung2.auth.model

/** 서버/로컬 공통으로 쓰는 사용자 프로필 모델 */
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String? = null
)
