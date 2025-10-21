// app/src/main/java/com/gamsung2/data/local/AppDatabase.kt
package com.gamsung2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room AppDatabase
 *
 * 포함 엔티티:
 *  - DailyWeather
 *  - FavoritePlaceEntity
 *  - EventEntity
 *
 * 주의:
 *  - 개발 단계에서는 fallbackToDestructiveMigration()로 파괴적 마이그레이션 허용
 *  - Converters.kt 가 없는 경우 @TypeConverters(Converters::class) 제거
 */
@Database(
    entities = [
        DailyWeather::class,
        FavoritePlaceEntity::class,
        EventEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class) // Converters 파일 없으면 이 줄 삭제
abstract class AppDatabase : RoomDatabase() {

    // --- DAO ---
    abstract fun dailyWeatherDao(): DailyWeatherDao
    abstract fun favoritePlaceDao(): FavoritePlaceDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** 싱글톤 인스턴스 */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        /** 기존 호출 호환용 */
        fun get(context: Context): AppDatabase = getInstance(context)

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "gamsung2.db"
            )
                // TODO: 출시 전 마이그레이션 추가 후 제거 권장
                .fallbackToDestructiveMigration()
                .build()
    }
}
