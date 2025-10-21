// app/src/main/java/com/gamsung2/network/HeaderInterceptor.kt
package com.gamsung2.network

import com.gamsung2.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val getAuthToken: () -> String? = { null },
    private val getApiKey: () -> String? = { BuildConfig.WEATHER_API_KEY }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val b = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        getApiKey()?.takeIf { it.isNotBlank() }?.let { b.header("X-Api-Key", it) }
        getAuthToken()?.takeIf { it.isNotBlank() }?.let { b.header("Authorization", "Bearer $it") }

        return chain.proceed(b.build())
    }
}
