// app/src/main/java/com/gamsung2/ui/map/MapScreen.kt
@file:OptIn(ExperimentalMaterial3Api::class)
package com.gamsung2.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private enum class ShareTarget { Kakao, Facebook, Line, Sms }

/* AppNav에서 onBack 콜백으로도, navController로도 쓸 수 있게 오버로드 제공 */
@Composable
fun MapScreen(onBack: () -> Unit) {
    val seoul = LatLng(37.5665, 126.9780)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(seoul, 12f)
    }
    val markerState = rememberMarkerState(position = seoul)

    val ctx = androidx.compose.ui.platform.LocalContext.current
    var hasLocationPerm by remember { mutableStateOf(isLocationGranted(ctx)) }
    var expanded by remember { mutableStateOf(false) }
    var pendingShare by remember { mutableStateOf<ShareTarget?>(null) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { r ->
        hasLocationPerm = (r[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (r[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        pendingShare?.let { tgt ->
            if (hasLocationPerm) {
                fetchCurrentLatLng(
                    ctx,
                    { shareNow(ctx, tgt, it) },
                    { shareNow(ctx, tgt, cameraState.position.target) }
                )
            } else {
                shareNow(ctx, tgt, cameraState.position.target)
            }
            pendingShare = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("지도") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // 내 위치 공유
                    Box {
                        FilledTonalButton(
                            onClick = { expanded = true },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) { Text("내 위치 공유", fontWeight = FontWeight.SemiBold) }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = DpOffset(0.dp, 8.dp)
                        ) {
                            fun go(t: ShareTarget) {
                                expanded = false
                                shareWithTarget(
                                    ctx, t, hasLocationPerm, cameraState, permLauncher
                                ) { pendingShare = it }
                            }
                            DropdownMenuItem(text = { Text("카카오톡") }, onClick = { go(ShareTarget.Kakao) })
                            DropdownMenuItem(text = { Text("페이스북") }, onClick = { go(ShareTarget.Facebook) })
                            DropdownMenuItem(text = { Text("라인") },     onClick = { go(ShareTarget.Line) })
                            DropdownMenuItem(text = { Text("문자") },     onClick = { go(ShareTarget.Sms) })
                        }
                    }
                }
            )
        }
    ) { inner ->
        GoogleMap(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            cameraPositionState = cameraState
        ) {
            Marker(state = markerState, title = "서울", snippet = "시청 근처")
        }
    }
}

/* NavController 버전도 바로 사용 가능 */
@Composable
fun MapScreen(navController: NavHostController) =
    MapScreen(onBack = { navController.popBackStack() })

/* ====== 공유/권한 유틸 ====== */
private fun shareWithTarget(
    ctx: Context,
    target: ShareTarget,
    hasPerm: Boolean,
    cameraState: CameraPositionState,
    permLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    setPending: (ShareTarget?) -> Unit
) {
    if (hasPerm) {
        fetchCurrentLatLng(
            ctx,
            { shareNow(ctx, target, it) },
            { shareNow(ctx, target, cameraState.position.target) }
        )
    } else {
        setPending(target)
        permLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

private fun shareNow(ctx: Context, target: ShareTarget, latLng: LatLng) {
    val url = "https://maps.google.com/?q=${latLng.latitude},${latLng.longitude}"
    val msg = "내 위치를 공유합니다:\n$url"
    when (target) {
        ShareTarget.Kakao    -> shareToPackage(ctx, msg, "com.kakao.talk")
        ShareTarget.Facebook -> shareToPackage(ctx, msg, "com.facebook.katana")
        ShareTarget.Line     -> shareToPackage(ctx, msg, "jp.naver.line.android")
        ShareTarget.Sms      -> shareToSms(ctx, msg)
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLatLng(
    ctx: Context,
    onOk: (LatLng) -> Unit,
    onFail: () -> Unit
) {
    try {
        LocationServices.getFusedLocationProviderClient(ctx).lastLocation
            .addOnSuccessListener { it?.let { loc -> onOk(LatLng(loc.latitude, loc.longitude)) } ?: onFail() }
            .addOnFailureListener { onFail() }
    } catch (_: Exception) {
        onFail()
    }
}

private fun isLocationGranted(ctx: Context) =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun shareToPackage(ctx: Context, text: String, pkg: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage(pkg)
    }
    if (intent.resolveActivity(ctx.packageManager) != null) ctx.startActivity(intent)
    else ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
}

private fun shareToSms(ctx: Context, text: String) {
    val uri = Uri.parse("smsto:")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply { putExtra("sms_body", text) }
    if (intent.resolveActivity(ctx.packageManager) != null) ctx.startActivity(intent)
}
