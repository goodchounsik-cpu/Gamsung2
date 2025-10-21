// app/src/main/java/com/gamsung2/ui/home/EventListSection.kt
package com.gamsung2.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.data.local.EventEntity
import com.gamsung2.viewmodel.EventViewModel
import java.util.Locale

@Composable
fun EventListSection(
    date: String,          // "yyyy-MM-dd"
    vm: EventViewModel,
) {
    // 선택 날짜를 VM에 반영 → vm.events 구독
    LaunchedEffect(date) { vm.setDate(date) }

    val events by vm.events.collectAsState(initial = emptyList())

    var showEditor by rememberSaveable { mutableStateOf(false) }

    // 🔧 Saver 제거: id만 저장하고, 리스트에서 찾아 복원
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing: EventEntity? = remember(editingId, events) {
        events.firstOrNull { it.id == editingId }
    }

    if (showEditor) {
        // 에디터는 분(Int?)을 넘기고, 여기서 "HH:mm" String?으로 변환해 VM에 전달
        EventEditorSheet(
            date = date,
            editing = editing,                    // null = 추가, not null = 수정
            onDismiss = { showEditor = false },
            onSave = { title, memo, startMinute, endMinute ->
                val startStr = minuteToHHMM(startMinute)
                val endStr   = minuteToHHMM(endMinute)

                if (editing == null) {
                    vm.add(
                        title = title,
                        date = date,
                        memo = memo,
                        allDay = (startStr == null && endStr == null),
                        startTime = startStr,
                        endTime = endStr
                    )
                } else {
                    vm.update(
                        origin = editing,
                        title = title,
                        memo = memo,
                        allDay = (startStr == null && endStr == null),
                        startTime = startStr,
                        endTime = endStr
                    )
                }
                showEditor = false
            },
            onDelete = { id ->
                vm.deleteEvent(id)
                showEditor = false
            }
        )
    }

    Column {
        // 상단 “추가” 버튼
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                editingId = null
                showEditor = true
            }) { Text("일정 추가") }
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("등록된 일정이 없습니다.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(items = events, key = { it.id }) { ev ->
                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(text = ev.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = timeRange(ev), style = MaterialTheme.typography.bodySmall)

                                if (ev.memo?.isNotBlank() == true) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = ev.memo.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    editingId = ev.id
                                    showEditor = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "수정")
                                }
                                IconButton(onClick = { vm.deleteEvent(ev.id) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "삭제",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Int? 분 -> "HH:mm" 문자열 (null이면 null) */
private fun minuteToHHMM(min: Int?): String? =
    min?.let { "%02d:%02d".format(it / 60, it % 60) }

/** allDay 이면 "종일", 아니면 "오전/오후 hh:mm ~ ..." */
private fun timeRange(e: EventEntity): String {
    if (e.allDay) return "종일"
    val s = e.startTime?.let { hhmmToPretty(it) } ?: "--:--"
    val t = e.endTime?.let { hhmmToPretty(it) } ?: "--:--"
    return "$s ~ $t"
}

/** "HH:mm" -> "오전/오후 hh:mm" (잘못된 입력도 안전 처리) */
private fun hhmmToPretty(hhmm: String): String {
    val parts = hhmm.split(":")
    val h24 = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val am = h24 < 12
    val h12 = (h24 % 12).let { if (it == 0) 12 else it }
    return "%s %02d:%02d".format(Locale.KOREA, if (am) "오전" else "오후", h12, m)
}
