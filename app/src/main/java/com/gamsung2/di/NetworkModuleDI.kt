// app/src/main/java/com/gamsung2/di/NetworkModuleDI.kt
package com.gamsung2.di

import com.gamsung2.remote.PlaceApi
import com.gamsung2.remote.weather.WeatherApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModuleDI {

    // Moshi
    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    // OkHttp (BuildConfig 의존 제거: 일단 BASIC 고정)
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Base URLs
    @Provides @Singleton @Named("baseUrlMain")
    fun provideBaseUrlMain(): String = "https://httpbin.org/"

    @Provides @Singleton @Named("baseUrlWeather")
    fun provideBaseUrlWeather(): String = "https://apis.data.go.kr/"

    // Retrofit: 메인
    @Provides @Singleton @Named("retrofitMain")
    fun provideRetrofitMain(
        okHttp: OkHttpClient,
        moshi: Moshi,
        @Named("baseUrlMain") baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Retrofit: 기상청
    @Provides @Singleton @Named("retrofitWeather")
    fun provideRetrofitWeather(
        okHttp: OkHttpClient,
        moshi: Moshi,
        @Named("baseUrlWeather") baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // APIs
    @Provides @Singleton
    fun providePlaceApi(@Named("retrofitMain") retrofit: Retrofit): PlaceApi =
        retrofit.create(PlaceApi::class.java)

    @Provides @Singleton
    fun provideWeatherApi(@Named("retrofitWeather") retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)
}
