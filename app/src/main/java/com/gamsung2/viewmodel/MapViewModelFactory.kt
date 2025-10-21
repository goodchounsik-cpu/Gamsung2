// app/src/main/java/com/gamsung2/viewmodel/MapViewModelFactory.kt
package com.gamsung2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gamsung2.MapViewModel            // ✅ 트리에 보이는 실제 위치: com.gamsung2
import com.gamsung2.data.local.AppDatabase
import com.gamsung2.repository.FavoritePlaceRepository

/**
 * MapViewModel 전용 Factory
 */
class MapViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // ❌ modelClass.isAssignableFrom(cls = ...)  ← named arg 쓰지 마세요
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val db = AppDatabase.getInstance(context)
            val favoriteDao = db.favoritePlaceDao()
            val repo = FavoritePlaceRepository(favoriteDao)
            return MapViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
