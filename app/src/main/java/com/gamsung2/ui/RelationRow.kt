package com.gamsung2.ui

import com.gamsung2.model.RelationState

data class RelationRow(
    val otherId: String,
    val stateForMe: RelationState,
    val primaryLabel: String? = null,
    val secondaryLabel: String? = null
    // 클릭 람다를 쓰려면 여기에 optional 람다 필드 추가 가능
)
