// app/src/main/java/com/gamsung2/relations/data/RelationRepository.kt
package com.gamsung2.relations.data

import com.gamsung2.model.RelationRow
import kotlinx.coroutines.flow.StateFlow

interface RelationRepository {
    val rows: StateFlow<List<RelationRow>>
    suspend fun boot(meId: String)
    suspend fun refresh()
    suspend fun sendRequest(targetPhone: String)
}
