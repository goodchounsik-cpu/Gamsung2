// app/src/main/java/com/gamsung2/viewmodel/WeatherViewModelFactory.kt
package com.gamsung2.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gamsung2.data.local.AppDatabase

class WeatherViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(WeatherViewModel::class.java) -> {
                // 1) Room DAO
                val db = AppDatabase.getInstance(context)
                val dao = db.dailyWeatherDao()   // ← 실제 DAO 함수명과 일치해야 함

                // 2) Retrofit API (FQCN으로 생성해 임포트 꼬임 방지)
                val api = com.gamsung2.remote.RetrofitProvider
                    .retrofit
                    .create(com.gamsung2.remote.weather.WeatherApi::class.java)

                // 3) Repository + VM (FQCN 사용)
                val repo = com.gamsung2.repository.WeatherRepository(dao, api)
                com.gamsung2.viewmodel.WeatherViewModel(repo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/** 헬퍼 */
fun weatherVmFactory(context: Context): WeatherViewModelFactory =
    WeatherViewModelFactory(context)
