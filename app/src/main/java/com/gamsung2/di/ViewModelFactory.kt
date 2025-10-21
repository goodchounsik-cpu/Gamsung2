package com.gamsung2.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gamsung2.MapViewModel
import com.gamsung2.data.local.AppDatabase
import com.gamsung2.repository.FavoritePlaceRepository

class ViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val dao = AppDatabase.getInstance(app).favoritePlaceDao()
            val repo = FavoritePlaceRepository(dao)            // ✅ DAO → Repository
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repo) as T                     // ✅ Repository 주입
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    companion object {
        fun provide(app: Application): ViewModelProvider.Factory = ViewModelFactory(app)
    }
}
