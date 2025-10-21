package com.gamsung2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime

/**
 * AM/PM 토글이 있는 간단한 시간 선택 UI
 * - 12시간제 입력 + 오전/오후 토글 -> LocalTime 반환
 */
@Composable
fun AmPmTimePicker(
    initial: LocalTime? = null,
    onTimeChange: (LocalTime?) -> Unit
) {
    var isAm by remember { mutableStateOf(initial?.hour?.let { it < 12 } ?: true) }
    var hourText by remember {
        mutableStateOf(
            (initial?.let { val h = it.hour % 12; if (h == 0) 12 else h } ?: 9).toString()
        )
    }
    var minuteText by remember { mutableStateOf(initial?.minute?.toString()?.padStart(2, '0') ?: "00") }

    fun notify() {
        val h12 = hourText.toIntOrNull()
        val m = minuteText.toIntOrNull()
        if (h12 == null || m == null || h12 !in 1..12 || m !in 0..59) {
            onTimeChange(null); return
        }
        val h24 = if (isAm) {
            if (h12 == 12) 0 else h12
        } else {
            if (h12 == 12) 12 else h12 + 12
        }
        onTimeChange(LocalTime.of(h24, m))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegmentedButtonAmPm(
                isAm = isAm,
                onChange = { isAm = it; notify() }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { hourText = it.filter { c -> c.isDigit() }.take(2); notify() },
                label = { Text("시(1~12)") },
                singleLine = true,
                modifier = Modifier.width(120.dp)
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { minuteText = it.filter { c -> c.isDigit() }.take(2); notify() },
                label = { Text("분(00~59)") },
                singleLine = true,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Composable
private fun SegmentedButtonAmPm(
    isAm: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row {
        FilterChip(
            selected = isAm,
            onClick = { onChange(true) },
            label = { Text("오전") }
        )
        Spacer(Modifier.width(8.dp))
        FilterChip(
            selected = !isAm,
            onClick = { onChange(false) },
            label = { Text("오후") }
        )
    }
}
