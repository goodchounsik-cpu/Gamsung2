// app/src/main/java/com/gamsung2/network/NetworkResult.kt
package com.gamsung2.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val cause: Throwable? = null
    ) : NetworkResult<Nothing>()
}
