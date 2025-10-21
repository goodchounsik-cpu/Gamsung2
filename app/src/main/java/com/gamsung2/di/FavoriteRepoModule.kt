package com.gamsung2.di

import com.gamsung2.data.local.FavoritePlaceDao
import com.gamsung2.repository.FavoritePlaceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FavoriteRepoModule {

    @Provides
    @Singleton
    fun provideFavoritePlaceRepository(
        dao: FavoritePlaceDao
    ): FavoritePlaceRepository = FavoritePlaceRepository(dao)
}
