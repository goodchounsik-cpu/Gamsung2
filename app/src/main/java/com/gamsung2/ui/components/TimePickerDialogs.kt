// app/src/main/java/com/gamsung2/ui/components/TimePickerDialogs.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties

@Composable
fun ClockTimePickerDialog(
    initialHour12: Int,           // 1..12
    initialMinute: Int,           // 0..59
    initialIsAm: Boolean,         // true=오전
    onDismiss: () -> Unit,
    onConfirm: (hour12: Int, minute: Int, isAm: Boolean) -> Unit
) {
    // ✅ Material3 시계형 TimePicker (드래그 가능)
    //   버전별 시그니처 차이를 피하려고 named args로 명시
    val state = rememberTimePickerState(
        initialHour = if (initialIsAm) {
            if (initialHour12 == 12) 0 else initialHour12
        } else {
            if (initialHour12 == 12) 12 else initialHour12 + 12
        },
        initialMinute = initialMinute,
        is24Hour = false
    )
    // state.hour: 0..23, state.minute: 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(onClick = {
                val h24 = state.hour
                val am = h24 < 12
                val h12 = when (val h = h24 % 12) { 0 -> 12; else -> h }
                onConfirm(h12, state.minute, am)
            }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        title = { Text("시간 선택") },
        text = {
            // ⏰ 시계 다이얼(드래그/탭 모두 가능)
            TimePicker(state = state)
        }
    )
}
