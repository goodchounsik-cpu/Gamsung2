package com.gamsung2.model

enum class RelationState { NONE, PENDING_SENT, PENDING_RECV, ACCEPTED, REJECTED }

data class Relation(
    val id: String,
    val fromId: String,
    val toId: String,
    val state: RelationState,
    val createdAt: Long
)
