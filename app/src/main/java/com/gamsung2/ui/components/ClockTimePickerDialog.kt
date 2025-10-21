// app/src/main/java/com/gamsung2/ui/components/ClockTimePickerDialog.kt
package com.gamsung2.ui.components

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.AlertDialog

/**
 * Material2 스타일(바늘 드래그형) TimePickerDialog를 Compose에서 래핑.
 *
 * @param title 다이얼 상단 제목
 * @param initialMinute 초기 분 값(null이면 09:00)
 * @param is24Hour 24시간제 여부
 * @param onDismiss 취소/닫기 콜백
 * @param onConfirm 선택 완료 시 (minuteOfDay: Int) 콜백 (0~1439 분)
 */
@Composable
fun ClockTimePickerDialog(
    title: String = "시간 선택",
    initialMinute: Int? = null,
    is24Hour: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (minuteOfDay: Int) -> Unit
) {
    val context = LocalContext.current

    // 초기 시간 계산
    val initHour = (initialMinute ?: 9 * 60) / 60
    val initMin = (initialMinute ?: 9 * 60) % 60

    // 이 컴포저블이 호출될 때 Android의 TimePickerDialog를 바로 띄움
    LaunchedEffect(Unit) {
        showTimePickerDialog(
            context = context,
            initHour = initHour,
            initMin = initMin,
            is24Hour = is24Hour,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }

    // Compose 쪽에는 아무 UI도 안 그리고 AlertDialog로 배경만 막아줌
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } },
        title = { Text(title) },
        text = { Text("시간 선택 다이얼이 표시됩니다…") }
    )
}

// Android의 TimePickerDialog 호출
private fun showTimePickerDialog(
    context: Context,
    initHour: Int,
    initMin: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (minuteOfDay: Int) -> Unit
) {
    val dialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onConfirm(hourOfDay * 60 + minute)
            onDismiss()
        },
        initHour,
        initMin,
        is24Hour
    )
    dialog.setOnCancelListener { onDismiss() }
    dialog.show()
}
