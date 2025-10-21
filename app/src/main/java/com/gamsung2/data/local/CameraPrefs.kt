package com.gamsung2.data.local

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("camera_prefs")

object CameraPrefs {
    private val LAT = doublePreferencesKey("lat")
    private val LNG = doublePreferencesKey("lng")
    private val ZOOM = doublePreferencesKey("zoom")

    fun flow(ctx: Context) = ctx.dataStore.data.map { p ->
        Triple(p[LAT] ?: 37.5665, p[LNG] ?: 126.9780, p[ZOOM] ?: 12.0) // 기본: 서울시청
    }

    suspend fun save(ctx: Context, lat: Double, lng: Double, zoom: Double) {
        ctx.dataStore.edit { p ->
            p[LAT] = lat; p[LNG] = lng; p[ZOOM] = zoom
        }
    }
}
