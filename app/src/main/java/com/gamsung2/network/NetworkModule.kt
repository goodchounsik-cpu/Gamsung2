// app/src/main/java/com/gamsung2/network/NetworkModule.kt
package com.gamsung2.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private fun logging(debug: Boolean): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (debug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }

    fun okHttp(debug: Boolean = false): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging(debug))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    fun retrofit(baseUrl: String, debug: Boolean = false): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp(debug))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
}
