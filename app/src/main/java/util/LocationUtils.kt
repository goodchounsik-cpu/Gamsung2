package com.gamsung2.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationUtils {

    /**
     * 현재 위치를 한 번 가져오는 서스펜드 함수.
     * - 퍼미션은 외부에서 보장해야 함 (FINE/COARSE)
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        // 빠른 one-shot: lastLocation 먼저 시도 → 없으면 getCurrentLocation 사용
        val last = runCatching { client.lastLocation.await() }.getOrNull()
        if (last != null) return last

        val priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
        return client.getCurrentLocation(priority, /* cancellationToken= */ null).await()
    }
}

/** Task<T>.await() 간단 확장 */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T? =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resume(null) }
        addOnCanceledListener { cont.resume(null) }
    }
