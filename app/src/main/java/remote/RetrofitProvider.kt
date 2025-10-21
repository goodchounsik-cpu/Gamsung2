// app/src/main/java/com/gamsung2/remote/RetrofitProvider.kt
package com.gamsung2.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitProvider {

    private const val BASE_URL = "https://httpbin.org/"

    // ---- Moshi ----
    private val moshiInstance: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // ---- OkHttp ----
    private val okHttpClientInternal: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // 필요하면 BODY 로 변경
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // ---- Retrofit ----
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientInternal)
            .addConverterFactory(MoshiConverterFactory.create(moshiInstance))
            .build()
    }

    // 호환용 getter (기존 코드에서 RetrofitProvider.okHttp / .moshi 사용 시)
    val okHttp: OkHttpClient get() = okHttpClientInternal
    val moshi: Moshi get() = moshiInstance

    // APIs
    val placeApi: PlaceApi by lazy { retrofit.create(PlaceApi::class.java) }
    // val weatherApi: WeatherApi by lazy { retrofit.create(WeatherApi::class.java) }
}
