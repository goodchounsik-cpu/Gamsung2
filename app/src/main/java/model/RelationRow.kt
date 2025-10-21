package com.gamsung2.model

data class RelationRow(
    val otherId: String,
    val stateForMe: RelationState,
    val primaryLabel: String? = null,
    val secondaryLabel: String? = null
)
