package com.gamsung2.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 앱 전역에 단 한 번만 선언되어야 하는 DataStore 위임자
private val Context.mapSettingsDataStore by preferencesDataStore(name = "map_settings")

class MapSettingsStore(private val context: Context) {

    private object Keys {
        val MAP_TYPE = intPreferencesKey("map_type")
        val TRAFFIC = booleanPreferencesKey("traffic")
    }

    /** 저장된 지도 유형 스트림 (기본 NORMAL) */
    val mapType: Flow<MapType> =
        context.mapSettingsDataStore.data.map { pref ->
            val raw = pref[Keys.MAP_TYPE] ?: MapType.NORMAL.ordinal
            MapType.values().getOrElse(raw) { MapType.NORMAL }
        }

    /** 저장된 교통표시 여부 스트림 (기본 false) */
    val trafficEnabled: Flow<Boolean> =
        context.mapSettingsDataStore.data.map { pref ->
            pref[Keys.TRAFFIC] ?: false
        }

    /** 지도 유형 저장 */
    suspend fun setMapType(type: MapType) {
        context.mapSettingsDataStore.edit { it[Keys.MAP_TYPE] = type.ordinal }
    }

    /** 교통표시 저장 */
    suspend fun setTrafficEnabled(enabled: Boolean) {
        context.mapSettingsDataStore.edit { it[Keys.TRAFFIC] = enabled }
    }
}
