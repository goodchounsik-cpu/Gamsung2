package com.gamsung2.di

import com.gamsung2.data.PlaceRepository
import com.gamsung2.remote.PlaceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides @Singleton
    fun providePlaceRepository(placeApi: PlaceApi): PlaceRepository =
        PlaceRepository.default(placeApi)
}
