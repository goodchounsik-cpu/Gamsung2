@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.gamsung2.ui.components.ChipBlock

private const val DIALOG_WIDTH_RATIO  = 0.86f
private const val DIALOG_HEIGHT_RATIO = 0.50f

private val ROW_GAP = 8.dp
private val COL_GAP = 10.dp

private val FS_LABEL = 12.sp
private val FS_INPUT = 11.sp
private val TITLE_FS = 16.sp
private val TITLE_HEIGHT = 56.dp

// 한 줄 높이(칩과 입력칸 정렬)
private val TIME_CELL_HEIGHT: Dp = 36.dp
// 라벨을 네모 박스 '바깥 위'로 살짝만 띄움 (필요하면 16~22dp 조절)
private val LABEL_OUT_OFFSET: Dp = 18.dp

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, memo: String?, startMinute: Int?, endMinute: Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    var amStart by remember { mutableStateOf(true) }
    var amEnd by remember { mutableStateOf(true) }
    var startHour by remember { mutableStateOf(9) }  // 1..12
    var startMin by remember { mutableStateOf(0) }   // 0..59
    var endHour by remember { mutableStateOf(9) }
    var endMin by remember { mutableStateOf(0) }

    fun h12ToTotalMin(am: Boolean, h12: Int, m: Int): Int {
        val h = when {
            am && h12 == 12 -> 0
            !am && h12 == 12 -> 12
            am -> h12
            else -> h12 + 12
        }
        return h * 60 + m
    }
    fun totalMinToH12(total: Int): Triple<Boolean, Int, Int> {
        val t = ((total % (24 * 60)) + (24 * 60)) % (24 * 60)
        val h24 = t / 60
        val m = t % 60
        val am = h24 < 12
        val h12 = when {
            h24 == 0 -> 12
            h24 == 12 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return Triple(am, h12, m)
    }
    fun fmt2(n: Int) = if (n in 0..9) "0$n" else n.toString()

    LaunchedEffect(amStart, startHour, startMin) {
        val startT = h12ToTotalMin(amStart, startHour, startMin)
        val suggest = startT + 30
        val (am2, h2, m2) = totalMinToH12(suggest)
        amEnd = am2
        endHour = h2
        endMin = m2
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(DIALOG_WIDTH_RATIO)
            .fillMaxHeight(DIALOG_HEIGHT_RATIO),
        title = {
            Text("일정 추가", style = MaterialTheme.typography.titleSmall.copy(fontSize = FS_LABEL))
        },
        text = {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(COL_GAP)
            ) {
                // 제목
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 200) title = it },
                    placeholder = {
                        Text(
                            "제목",
                            fontSize = TITLE_FS,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = TITLE_FS, textAlign = TextAlign.Center),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TITLE_HEIGHT)
                )

                // 시작 시간
                Text("시작 시간", style = MaterialTheme.typography.labelSmall.copy(fontSize = FS_LABEL))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.height(TIME_CELL_HEIGHT), contentAlignment = Alignment.Center) {
                        ChipBlock(isAm = amStart, onAm = { amStart = true }, onPm = { amStart = false })
                    }
                    TimeCell(
                        label = "시",
                        value = startHour,
                        onValue = { startHour = it.coerceIn(1, 12) },
                        display = { fmt2(it) },
                        range = 1..12,
                        boxWidth = 66.dp
                    )
                    TimeCell(
                        label = "분",
                        value = startMin,
                        onValue = { startMin = it.coerceIn(0, 59) },
                        display = { fmt2(it) },
                        range = 0..59,
                        boxWidth = 66.dp
                    )
                }

                // 종료 시간
                Text("종료 시간", style = MaterialTheme.typography.labelSmall.copy(fontSize = FS_LABEL))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.height(TIME_CELL_HEIGHT), contentAlignment = Alignment.Center) {
                        ChipBlock(isAm = amEnd, onAm = { amEnd = true }, onPm = { amEnd = false })
                    }
                    TimeCell(
                        label = "시",
                        value = endHour,
                        onValue = { endHour = it.coerceIn(1, 12) },
                        display = { fmt2(it) },
                        range = 1..12,
                        boxWidth = 66.dp
                    )
                    TimeCell(
                        label = "분",
                        value = endMin,
                        onValue = { endMin = it.coerceIn(0, 59) },
                        display = { fmt2(it) },
                        range = 0..59,
                        boxWidth = 66.dp
                    )
                }

                // 메모
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모", fontSize = FS_LABEL) },
                    textStyle = LocalTextStyle.current.copy(fontSize = FS_INPUT),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default
                )

                // 하단 버튼
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = title.isNotBlank(),
                        onClick = {
                            val s = h12ToTotalMin(amStart, startHour, startMin)
                            var e = h12ToTotalMin(amEnd, endHour, endMin)
                            if (e < s) e = s
                            onSave(title.trim(), memo.ifBlank { null }, s, e)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
                    ) { Text("추가", fontSize = FS_INPUT) }

                    OutlinedButton(
                        onClick = {}, enabled = false,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
                    ) { Text("수정", fontSize = FS_INPUT) }

                    FilledTonalButton(
                        onClick = {}, enabled = false,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
                    ) { Text("저장", fontSize = FS_INPUT) }

                    OutlinedButton(
                        onClick = {}, enabled = false,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
                    ) { Text("삭제", fontSize = FS_INPUT) }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
                    ) { Text("취소", fontSize = FS_INPUT) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

/* -------------------- 내부 컴포넌트 -------------------- */
@Composable
private fun TimeCell(
    label: String,
    value: Int,
    onValue: (Int) -> Unit,
    range: IntRange,
    boxWidth: Dp = 66.dp,
    display: (Int) -> String = { it.toString() }
) {
    Box(
        modifier = Modifier
            .width(boxWidth)
            .height(TIME_CELL_HEIGHT)
    ) {
        var text by remember(value) { mutableStateOf(display(value)) }
        LaunchedEffect(value) { text = display(value) }

        OutlinedTextField(
            value = text,
            onValueChange = { t ->
                val cleaned = t.filter { it.isDigit() }
                if (cleaned.isEmpty()) {
                    text = ""
                } else {
                    val n = cleaned.toInt()
                    val c = when {
                        n < range.first -> range.first
                        n > range.last  -> range.last
                        else            -> n
                    }
                    onValue(c)
                    text = display(c)
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = FS_INPUT, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .align(Alignment.Center)
                .width(boxWidth)
                .height(TIME_CELL_HEIGHT)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = FS_LABEL),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -LABEL_OUT_OFFSET)   // ← 라벨과 박스 간격
                .width(boxWidth)
        )
    }
}
