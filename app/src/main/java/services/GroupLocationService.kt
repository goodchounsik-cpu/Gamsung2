// app/src/main/java/com/gamsung2/services/GroupLocationService.kt
package com.gamsung2.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.gamsung2.R
import com.gamsung2.data.local.GroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 가족/그룹 위치를 주기적으로 파이어스토어에 업데이트하는 Foreground Service
 */
class GroupLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val repo = GroupRepository()
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(1, buildNotification("위치 서비스 실행 중…"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 그룹 코드 받아오기 (필요 시)
        val code = intent?.getStringExtra("groupCode") ?: return START_NOT_STICKY
        startLocationUpdates(code)
        return START_STICKY
    }

    private fun startLocationUpdates(groupCode: String) {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L // 10초 간격
        ).build()

        fusedLocationClient.requestLocationUpdates(
            request,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc: Location = result.lastLocation ?: return
                    serviceScope.launch {
                        // 배터리 퍼센트는 임의로 100으로
                        repo.updateLocation(groupCode, loc.latitude, loc.longitude, 100)
                    }
                }
            },
            mainLooper
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "group_location", "그룹 위치 서비스",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, "group_location")
            .setContentTitle("그룹 위치 공유")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
