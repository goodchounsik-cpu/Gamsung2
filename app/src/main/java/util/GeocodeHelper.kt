// app/src/main/java/com/gamsung2/util/GeocodeHelper.kt
package com.gamsung2.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object GeocodeHelper {

    /** 주소/장소 문자열을 좌표 하나로 변환 */
    suspend fun geocodeFirst(context: Context, query: String): LatLng? {
        if (query.isBlank()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        val q = query.trim()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ : 비동기 콜백 API
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(q, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val a = addresses.firstOrNull()
                        cont.resume(a?.let { LatLng(it.latitude, it.longitude) })
                    }
                    override fun onError(errorMessage: String?) {
                        cont.resume(null)
                    }
                })
            }
        } else {
            // API 32- : 동기 API (IO 스레드에서)
            withContext(Dispatchers.IO) {
                try {
                    val list = geocoder.getFromLocationName(q, 1)
                    val a = list?.firstOrNull()
                    a?.let { LatLng(it.latitude, it.longitude) }
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }

    /** 좌표를 첫 번째 주소 문자열로 변환 */
    suspend fun reverseFirst(context: Context, lat: Double, lng: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ : 비동기 콜백 API
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val line = addresses.firstOrNull()?.getAddressLine(0)
                        cont.resume(line)
                    }
                    override fun onError(errorMessage: String?) {
                        cont.resume(null)
                    }
                })
            }
        } else {
            // API 32- : 동기 API (IO 스레드에서)
            withContext(Dispatchers.IO) {
                try {
                    geocoder.getFromLocation(lat, lng, 1)
                        ?.firstOrNull()
                        ?.getAddressLine(0)
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }
}
