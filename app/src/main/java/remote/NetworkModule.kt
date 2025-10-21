// app/src/main/java/com/gamsung2/remote/NetworkModule.kt
package com.gamsung2.remote

import com.gamsung2.BuildConfig
import com.gamsung2.network.HeaderInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * OkHttp/Retrofit 공통 구성.
 * 기존 RetrofitProvider 가 있다면 이 클래스로 대체하거나 내부 구현을 이 방식으로 바꿔줘.
 */
object NetworkModule {

    // ⚙️ Moshi
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // ⚙️ 로그
    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        }
    }

    // ⚙️ 헤더 인터셉터 (토큰 공급자가 있으면 람다 주입 가능)
    private val headerInterceptor = HeaderInterceptor()

    // ⚙️ OkHttpClient
    val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // ⚙️ Retrofit 생성기
    fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
}
