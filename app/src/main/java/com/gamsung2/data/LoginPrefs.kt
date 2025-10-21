package com.gamsung2.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "login_prefs")

object LoginPrefs {
    private val KEY_REMEMBER_ID = booleanPreferencesKey("remember_id")
    private val KEY_LAST_ID     = stringPreferencesKey("last_id")

    /** 한번에 읽기 (초기 진입 시 사용) */
    suspend fun readOnce(context: Context): Pair<Boolean, String> {
        val prefs = context.dataStore.data.first()
        val remember = prefs[KEY_REMEMBER_ID] ?: false
        val id = if (remember) (prefs[KEY_LAST_ID] ?: "") else ""
        return remember to id
    }

    /** 저장 (로그인 성공 시 호출) */
    suspend fun save(context: Context, remember: Boolean, id: String?) {
        context.dataStore.edit { p ->
            p[KEY_REMEMBER_ID] = remember
            if (remember && !id.isNullOrBlank()) {
                p[KEY_LAST_ID] = id
            } else {
                p.remove(KEY_LAST_ID)
            }
        }
    }
}
