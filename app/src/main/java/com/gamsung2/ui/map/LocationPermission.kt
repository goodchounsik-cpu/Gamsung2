package com.gamsung2.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun rememberLocationPermissionRequester(
    onGranted: () -> Unit,
    onDenied: (permanently: Boolean) -> Unit
): () -> Unit {
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fine = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fine || coarse) {
            onGranted()
        } else {
            // 영구 거부 추정: shouldShowRequestPermissionRationale이 모두 false면 영구 거부로 간주
            val act = ctx.findActivity()
            val fineRationale = act?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: false
            val coarseRationale = act?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } ?: false
            val permanentlyDenied = !(fineRationale || coarseRationale)
            onDenied(permanentlyDenied)
        }
    }

    return {
        val hasFine = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            onGranted()
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

/** Context를 Activity로 변환하는 유틸 */
private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
