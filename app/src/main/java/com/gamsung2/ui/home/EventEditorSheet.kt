// app/src/main/java/com/gamsung2/ui/home/EventEditorSheet.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.data.local.EventEntity

@Composable
fun EventEditorSheet(
    date: String,
    editing: EventEntity?, // null=추가, not null=수정
    onDismiss: () -> Unit,
    onSave: (title: String, memo: String?, startMinute: Int?, endMinute: Int?) -> Unit,
    onDelete: (id: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember(editing) { mutableStateOf(editing?.title.orEmpty()) }
    var memo by remember(editing) { mutableStateOf(editing?.memo.orEmpty()) }
    var startMinute by remember(editing) { mutableStateOf(hhmmToMinute(editing?.startTime)) }
    var endMinute by remember(editing) { mutableStateOf(hhmmToMinute(editing?.endTime)) }

    val isEdit = editing != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEdit) "일정 수정" else "일정 추가",
                style = MaterialTheme.typography.titleLarge
            )
            Text("날짜: $date", style = MaterialTheme.typography.labelLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text("메모 (선택)") },
                modifier = Modifier.fillMaxWidth()
            )

            TimeRow(
                startMinute = startMinute,
                endMinute = endMinute,
                onChange = { s, e ->
                    startMinute = s
                    endMinute = e
                }
            )

            Spacer(Modifier.height(8.dp))

            // ── 버튼 영역 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("취소") }

                if (isEdit) {
                    OutlinedButton(
                        onClick = { onDelete(editing!!.id) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("삭제")
                    }
                    Button(
                        onClick = {
                            onSave(
                                title.trim(),
                                memo.trim().ifBlank { null },
                                startMinute,
                                endMinute
                            )
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("수정") }
                } else {
                    Button(
                        onClick = {
                            onSave(
                                title.trim(),
                                memo.trim().ifBlank { null },
                                startMinute,
                                endMinute
                            )
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(2f)
                    ) { Text("저장") }
                }
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun TimeRow(
    startMinute: Int?,
    endMinute: Int?,
    onChange: (start: Int?, end: Int?) -> Unit
) {
    fun asText(v: Int?) = v?.toString().orEmpty()
    var s by remember(startMinute) { mutableStateOf(asText(startMinute)) }
    var e by remember(endMinute) { mutableStateOf(asText(endMinute)) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("시간(선택): 분 단위 입력 (예: 540 = 09:00)", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = s,
                onValueChange = {
                    s = it
                    onChange(it.toIntOrNull(), e.toIntOrNull())
                },
                label = { Text("시작 분") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = e,
                onValueChange = {
                    e = it
                    onChange(s.toIntOrNull(), it.toIntOrNull())
                },
                label = { Text("종료 분") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        val pretty =
            if (s.isBlank() && e.isBlank()) "종일"
            else "${minuteToHHMM(s.toIntOrNull()) ?: "--:--"} ~ ${minuteToHHMM(e.toIntOrNull()) ?: "--:--"}"
        Text(pretty, style = MaterialTheme.typography.bodySmall)
    }
}

/* 변환 유틸 */
private fun hhmmToMinute(hhmm: String?): Int? {
    if (hhmm.isNullOrBlank()) return null
    val p = hhmm.split(":")
    val h = p.getOrNull(0)?.toIntOrNull() ?: return null
    val m = p.getOrNull(1)?.toIntOrNull() ?: 0
    return h * 60 + m
}
private fun minuteToHHMM(min: Int?): String? =
    min?.let { "%02d:%02d".format(it / 60, it % 60) }
