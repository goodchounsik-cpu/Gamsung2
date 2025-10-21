// app/src/main/java/com/gamsung2/data/local/DbProvider.kt
package com.gamsung2.data.local

import android.content.Context

/**
 * 간단한 DB/DAO 제공자.
 * - Application Context로 Room 싱글톤을 얻어와서 캐싱해 둡니다.
 * - Hilt를 쓰지 않는 프로젝트에서 가볍게 사용하기 좋습니다.
 */
object DbProvider {

    @Volatile
    private var dbRef: AppDatabase? = null

    /** AppDatabase 싱글톤 얻기 */
    fun db(context: Context): AppDatabase =
        dbRef ?: synchronized(this) {
            dbRef ?: AppDatabase.getInstance(context.applicationContext).also { dbRef = it }
        }

    // ---- DAO short-cuts ----
    fun dailyWeatherDao(context: Context) = db(context).dailyWeatherDao()
    fun favoritePlaceDao(context: Context) = db(context).favoritePlaceDao()
    fun eventDao(context: Context) = db(context).eventDao()
}
