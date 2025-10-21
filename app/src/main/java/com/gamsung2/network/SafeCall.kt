// app/src/main/java/com/gamsung2/network/SafeCall.kt
package com.gamsung2.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody
import retrofit2.HttpException

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    val code: Int? = null,
    val message: String? = null
)

suspend inline fun <T> safeApiCall(
    moshi: Moshi,
    crossinline block: suspend () -> T
): NetworkResult<T> {
    return try {
        val data = block()
        NetworkResult.Success(data)
    } catch (ce: CancellationException) {
        throw ce
    } catch (he: HttpException) {
        val body = he.response()?.errorBody()
        val parsed = body.parseErrorBody(moshi)
        NetworkResult.Error(code = he.code(), message = parsed?.message ?: he.message(), cause = he)
    } catch (t: Throwable) {
        NetworkResult.Error(message = t.message, cause = t)
    }
}

@PublishedApi
internal fun ResponseBody?.parseErrorBody(moshi: Moshi): ErrorResponse? {
    if (this == null) return null
    return runCatching {
        moshi.adapter(ErrorResponse::class.java).fromJson(string())
    }.getOrNull()
}
