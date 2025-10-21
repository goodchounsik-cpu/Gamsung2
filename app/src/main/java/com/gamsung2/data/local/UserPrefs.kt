package com.gamsung2.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * 단말 내부에 간단한 상태를 저장/복원하는 DataStore 래퍼.
 * - 홈 화면: 마지막 지역/모드/선택일
 * - 테마 화면: 마지막 테마/그룹/지역
 *
 * 날짜는 ISO-8601 문자열(yyyy-MM-dd)로 저장합니다.
 */
class UserPrefs(private val context: Context) {

    /** ---------------- Keys ---------------- */
    private object K {
        val HOME_REGION = stringPreferencesKey("home_region")          // ex) "서울"
        val HOME_MODE   = stringPreferencesKey("home_mode")            // "Nearby" | "Nationwide"
        val HOME_DATE   = stringPreferencesKey("home_date")            // ISO LocalDate

        val THEME_KEY     = stringPreferencesKey("theme_key_last")     // ex) "history"
        val THEME_GROUP   = stringPreferencesKey("theme_group_last")   // ex) "가족"
        val THEME_REGION  = stringPreferencesKey("theme_region_last")  // ex) "서울"
    }

    /** ---------------- Flows (read) ---------------- */
    val lastRegionHome: Flow<String> = context.dataStore.data.map { it[K.HOME_REGION] ?: "서울" }
    val lastModeHome:   Flow<String> = context.dataStore.data.map { it[K.HOME_MODE] ?: "Nearby" }
    val lastDateHome:   Flow<LocalDate> =
        context.dataStore.data.map { prefs ->
            prefs[K.HOME_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        }

    val lastThemeKey:   Flow<String> = context.dataStore.data.map { it[K.THEME_KEY] ?: "history" }
    val lastThemeGroup: Flow<String> = context.dataStore.data.map { it[K.THEME_GROUP] ?: "가족" }
    val lastThemeRegion:Flow<String> = context.dataStore.data.map { it[K.THEME_REGION] ?: "서울" }

    /** ---------------- Write APIs ---------------- */
    suspend fun setHomeRegion(region: String) {
        context.dataStore.edit { it[K.HOME_REGION] = region }
    }

    suspend fun setHomeMode(mode: String) {
        // "Nearby" | "Nationwide" 로 저장
        context.dataStore.edit { it[K.HOME_MODE] = mode }
    }

    suspend fun setHomeDate(date: LocalDate) {
        context.dataStore.edit { it[K.HOME_DATE] = date.toString() }
    }

    suspend fun setThemeKey(key: String) {
        context.dataStore.edit { it[K.THEME_KEY] = key }
    }

    suspend fun setThemeGroup(group: String) {
        context.dataStore.edit { it[K.THEME_GROUP] = group }
    }

    suspend fun setThemeRegion(region: String) {
        context.dataStore.edit { it[K.THEME_REGION] = region }
    }
}

/** 앱 전역 DataStore 인스턴스 */
private val Context.dataStore by preferencesDataStore(name = "user_prefs")
