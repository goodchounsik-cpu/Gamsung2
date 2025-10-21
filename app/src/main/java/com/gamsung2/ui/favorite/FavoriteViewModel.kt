// app/src/main/java/com/gamsung2/ui/favorite/FavoriteViewModel.kt
package com.gamsung2.ui.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.local.DbProvider
import com.gamsung2.data.local.FavoritePlaceEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(app: Application) : AndroidViewModel(app) {

    // DAO 직접 사용 (Hilt 없이 간단하게)
    private val dao = DbProvider.favoritePlaceDao(app)

    // 즐겨찾기 목록 스트림
    val favorites: StateFlow<List<FavoritePlaceEntity>> =
        dao.getAllFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * 즐겨찾기 토글
     * - 같은 placeId가 있으면 삭제, 없으면 추가
     */
    fun toggle(entity: FavoritePlaceEntity) = viewModelScope.launch {
        val exists = favorites.value.any { it.placeId == entity.placeId }
        if (exists) {
            dao.deleteByPlaceId(entity.placeId)
        } else {
            dao.upsert(entity.copy(id = 0)) // autoGenerate
        }
    }
}
