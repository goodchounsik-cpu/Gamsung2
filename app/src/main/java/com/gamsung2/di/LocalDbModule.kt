package com.gamsung2.di

import android.content.Context
import androidx.room.Room
import com.gamsung2.data.local.AppDatabase
import com.gamsung2.data.local.FavoritePlaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDbModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gamsung.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoritePlaceDao(db: AppDatabase): FavoritePlaceDao =
        db.favoritePlaceDao()
}
