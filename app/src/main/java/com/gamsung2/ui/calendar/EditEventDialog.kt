@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.calendar  // ← 프로젝트 구조에 맞게: ui/event 라면 ui.event 로 바꾸기

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.gamsung2.data.local.EventEntity
import com.gamsung2.ui.components.ChipBlock
import com.gamsung2.ui.components.TimeFieldsCompact

private const val DIALOG_WIDTH_RATIO  = 0.86f
private const val DIALOG_HEIGHT_RATIO = 0.50f
private val ROW_GAP = 4.dp
private val COL_GAP = 6.dp
private val FS = 12.sp
private val TITLE_FS = 16.sp
private val TITLE_HEIGHT = 56.dp

@Composable
fun EditEventDialog(
    event: EventEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, memo: String?, startHHmm: String?, endHHmm: String?) -> Unit,
    onDelete: (id: Long) -> Unit
) {
    // ✅ 로컬 상태는 전부 non-null String 으로 (OutlinedTextField value 타입 맞추기)
    var title by remember(event.id) { mutableStateOf(event.title) }
    var memo  by remember(event.id) { mutableStateOf(event.memo ?: "") }

    fun parse(hhmm: String?): Pair<Int, Int>? = hhmm?.split(":")?.let {
        val h = it.getOrNull(0)?.toIntOrNull() ?: return@let null
        val m = it.getOrNull(1)?.toIntOrNull() ?: return@let null
        h to m
    }
    val (startH24, startM) = parse(event.startTime) ?: (9 to 0)
    val (endH24,   endM)   = parse(event.endTime)   ?: (9 to 0)

    var amStart  by remember(event.id) { mutableStateOf(startH24 < 12) }
    var amEnd    by remember(event.id) { mutableStateOf(endH24   < 12) }
    var startHour by remember(event.id) { mutableStateOf(((startH24 - 1 + 12) % 12) + 1) }
    var startMin  by remember(event.id) { mutableStateOf(startM) }
    var endHour   by remember(event.id) { mutableStateOf(((endH24   - 1 + 12) % 12) + 1) }
    var endMin    by remember(event.id) { mutableStateOf(endM) }

    fun toHHmm(am: Boolean, h12: Int, m: Int): String {
        val h24 = when {
            am && h12 == 12 -> 0
            !am && h12 == 12 -> 12
            am -> h12
            else -> h12 + 12
        }
        return "%02d:%02d".format(h24, m)
    }

    AlertDialog(
        onDismiss,                                 // 함수타입 파라미터는 이름 없이 위치 인자!
        confirmButton = { }, dismissButton = { },  // 빈 슬롯
        modifier = Modifier
            .fillMaxWidth(DIALOG_WIDTH_RATIO)
            .fillMaxHeight(DIALOG_HEIGHT_RATIO),
        title = { Text("일정 수정", style = MaterialTheme.typography.titleSmall.copy(fontSize = FS)) },
        text = {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(COL_GAP)
            ) {
                // 제목
                OutlinedTextField(
                    value = title,                                 // String
                    onValueChange = { if (it.length <= 200) title = it },
                    placeholder = {
                        Text("제목", fontSize = TITLE_FS, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = TITLE_FS, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth().height(TITLE_HEIGHT)
                )

                // 시작 시간
                Text("시작 시간", style = MaterialTheme.typography.labelSmall.copy(fontSize = FS))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(ROW_GAP), Alignment.CenterVertically) {
                    ChipBlock(isAm = amStart, onAm = { amStart = true }, onPm = { amStart = false })
                    TimeFieldsCompact(
                        hour = startHour, onHour = { startHour = it.coerceIn(1, 12) },
                        minute = startMin, onMinute = { startMin = it.coerceIn(0, 59) }
                    )
                }

                // 종료 시간
                Text("종료 시간", style = MaterialTheme.typography.labelSmall.copy(fontSize = FS))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(ROW_GAP), Alignment.CenterVertically) {
                    ChipBlock(isAm = amEnd, onAm = { amEnd = true }, onPm = { amEnd = false })
                    TimeFieldsCompact(
                        hour = endHour, onHour = { endHour = it.coerceIn(1, 12) },
                        minute = endMin, onMinute = { endMin = it.coerceIn(0, 59) }
                    )
                }

                // 메모 (value는 반드시 String)
                OutlinedTextField(
                    value = memo,                                  // String
                    onValueChange = { memo = it },
                    label = { Text("메모", fontSize = FS) },
                    textStyle = LocalTextStyle.current.copy(fontSize = FS),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                    OutlinedButton({ }, enabled = false, modifier = Modifier.weight(1f).height(36.dp)) { Text("추가", fontSize = FS) }
                    Button({ }, modifier = Modifier.weight(1f).height(36.dp)) { Text("수정", fontSize = FS) }

                    FilledTonalButton(
                        {
                            val s = toHHmm(amStart, startHour, startMin)
                            val e = toHHmm(amEnd,   endHour,   endMin)
                            onSave(
                                title.trim(),                       // ✅ 함수타입: 전부 위치 인자
                                memo.takeIf { it.isNotBlank() },    // 공백이면 null
                                s,
                                e
                            )
                            onDismiss()
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) { Text("저장", fontSize = FS) }

                    OutlinedButton({ onDelete(event.id) }, modifier = Modifier.weight(1f).height(36.dp)) { Text("삭제", fontSize = FS) }
                    TextButton(onDismiss, modifier = Modifier.weight(1f).height(36.dp)) { Text("취소", fontSize = FS) }
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}
