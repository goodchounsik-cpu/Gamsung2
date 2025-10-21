// app/src/main/java/com/gamsung2/data/SessionManager.kt
package com.gamsung2.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 앱 전역 DataStore (파일명: session_prefs)
private val Context.dataStore by preferencesDataStore("session_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_TOKEN = stringPreferencesKey("auth_token")
        val KEY_USER_TYPE = stringPreferencesKey("user_type") // general / biz / gov
    }

    /** 관찰용 Flow */
    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val userTypeFlow: Flow<String?> = context.dataStore.data.map { it[KEY_USER_TYPE] }

    /** 즉시 읽기(suspend) API */
    suspend fun getToken(): String? = tokenFlow.first()
    suspend fun getUserType(): String? = userTypeFlow.first()
    suspend fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    /** 저장/갱신 */
    suspend fun saveSession(token: String, userType: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            userType?.let { prefs[KEY_USER_TYPE] = it }
        }
    }

    suspend fun updateToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    /** 로그아웃(토큰만 제거) */
    suspend fun logout() {
        context.dataStore.edit {
            it.remove(KEY_TOKEN)
            // 유저 타입은 유지하려면 주석 유지, 함께 지우려면 다음 줄 사용
            // it.remove(KEY_USER_TYPE)
        }
    }

    /** 전체 초기화 */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
