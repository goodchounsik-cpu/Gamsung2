@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.gamsung2.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import java.time.*
import kotlin.math.max

enum class MemberDialogMode { ADD, EDIT }
data class GroupSchedule(val startAt: Long?, val endAt: Long?)

/* ───────── 메인 다이얼로그 ───────── */
@Composable
fun AddGroupMemberDialog(
    mode: MemberDialogMode = MemberDialogMode.ADD,
    initialPhone: String? = null,
    onDismiss: () -> Unit,
    onAddOrSave: (phone: String, schedule: GroupSchedule?) -> Unit,
    existingPhones: List<String> = emptyList(),
    currentSchedule: GroupSchedule? = null,
    onEditRequest: ((index: Int) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDisbandGroup: (() -> Unit)? = null
) {
    var phone by remember(initialPhone) { mutableStateOf(initialPhone.orEmpty()) }
    val trimmed by remember { derivedStateOf { phone.trim() } }
    val canTypeSave = trimmed.isNotEmpty()

    /* ✅ 타임존을 명시적으로 고정 (원하면 systemDefault로 바꿔도 OK) */
    val zone: ZoneId = remember { ZoneId.of("Asia/Seoul") }

    val now0 = remember { LocalDateTime.now(zone) }
    val defaultStart = remember { now0.atZone(zone).toInstant().toEpochMilli() }
    val defaultEnd   = remember { now0.plusMinutes(5).atZone(zone).toInstant().toEpochMilli() }

    var startAt by remember(currentSchedule) { mutableStateOf(currentSchedule?.startAt ?: defaultStart) }
    var endAt   by remember(currentSchedule) { mutableStateOf(currentSchedule?.endAt   ?: defaultEnd) }

    /* ✅ 종료시간 기준 남은 시간: endAt - 현재(Instant.now) */
    val remainingText by produceState(initialValue = "", endAt) {
        while (isActive) {
            val nowMs = Instant.now().toEpochMilli()
            val left  = max(0L, endAt - nowMs)
            value = "남은 시간 ${formatDuration(left)}"
            kotlinx.coroutines.delay(1000)
        }
    }

    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (mode == MemberDialogMode.ADD) "그룹에 전화번호 추가" else "전화번호 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("전화번호", fontSize = 11.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 36.dp)
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canTypeSave) {
                                onAddOrSave(trimmed, null)
                                phone = ""
                                focusRequester.requestFocus()
                            }
                        }
                    )
                )

                if (existingPhones.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        existingPhones.forEachIndexed { idx, p ->
                            AssistChip(
                                onClick = { onEditRequest?.invoke(idx) },
                                label = { Text(p) },
                                enabled = onEditRequest != null
                            )
                        }
                    }
                }

                /* ── 시작일 ── */
                Text("시작일", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp)
                InlineDateTimeLine(
                    valueMs = startAt,
                    zone = zone,
                    onChange = { ms ->
                        startAt = ms
                        if (startAt > endAt) endAt = startAt   // 보정
                    },
                    dense = true
                )

                /* ── 종료일 ── */
                Text("종료일", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp)
                InlineDateTimeLine(
                    valueMs = endAt,
                    zone = zone,
                    onChange = { ms ->
                        endAt = ms
                        if (endAt < startAt) startAt = endAt   // 보정
                    },
                    dense = true
                )

                AssistChip(onClick = {}, enabled = false, label = { Text(remainingText) })
            }
        },
        dismissButton = {
            val ok = startAt <= endAt
            TextButton(enabled = ok, onClick = {
                onAddOrSave(trimmed, GroupSchedule(startAt, endAt))
                onDismiss()
            }) { Text("저장") }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(enabled = onDelete != null, onClick = { onDelete?.invoke(); onDismiss() }) { Text("삭제") }
                TextButton(enabled = onDisbandGroup != null, onClick = { onDisbandGroup?.invoke(); onDismiss() }) { Text("해제") }
                TextButton(
                    enabled = canTypeSave,
                    onClick = {
                        onAddOrSave(trimmed, null)
                        phone = ""
                        focusRequester.requestFocus()
                    }
                ) { Text(if (mode == MemberDialogMode.ADD) "추가" else "저장(즉시)") }
            }
        }
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

/* ───────── 인라인 Date/Time ───────── */
@Composable
private fun InlineDateTimeLine(
    valueMs: Long,
    zone: ZoneId,
    onChange: (Long) -> Unit,
    dense: Boolean = true,
) {
    val dt0 = remember(valueMs, zone) { Instant.ofEpochMilli(valueMs).atZone(zone).toLocalDateTime() }

    var yy by remember(valueMs, zone) { mutableStateOf(dt0.year.toString()) }
    var mm by remember(valueMs, zone) { mutableStateOf(dt0.monthValue.toString().padStart(2, '0')) }
    var dd by remember(valueMs, zone) { mutableStateOf(dt0.dayOfMonth.toString().padStart(2, '0')) }
    var hh by remember(valueMs, zone) { mutableStateOf(dt0.hour.toString().padStart(2, '0')) }
    var mi by remember(valueMs, zone) { mutableStateOf(dt0.minute.toString().padStart(2, '0')) }

    LaunchedEffect(yy, mm, dd, hh, mi, zone) {
        if (yy.length == 4 && mm.length == 2 && dd.length == 2 && hh.length == 2 && mi.length == 2) {
            toMillisSafe(yy, mm, dd, hh, mi, zone)?.let(onChange)
        }
    }

    val gap = if (dense) 2.dp else 6.dp
    val sp: TextUnit  = if (dense) 14.sp else 16.sp
    val w4: Dp        = if (dense) 46.dp else 56.dp
    val w2: Dp        = if (dense) 28.dp else 34.dp
    val color: Color  = MaterialTheme.colorScheme.primary

    val numStyle = TextStyle(
        fontSize = sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        color = color,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        fontFeatureSettings = "tnum"
    )
    val txtStyle = TextStyle(
        fontSize = sp,
        lineHeight = 20.sp,
        color = color,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        NumSeg(yy,  { yy = it }, w4, numStyle, 4, clearOnFocus = true, padOnBlur = 4, cursorColor = color); StaticText("년 /", txtStyle)
        NumSeg(mm,  { mm = it }, w2, numStyle, 2, clearOnFocus = true, padOnBlur = 2, cursorColor = color); StaticText("월",   txtStyle)
        NumSeg(dd,  { dd = it }, w2, numStyle, 2, clearOnFocus = true, padOnBlur = 2, cursorColor = color); StaticText("일 /", txtStyle)
        NumSeg(hh,  { hh = it }, w2, numStyle, 2, clearOnFocus = true, padOnBlur = 2, cursorColor = color); StaticText("시",   txtStyle)
        NumSeg(mi,  { mi = it }, w2, numStyle, 2, clearOnFocus = true, padOnBlur = 2, cursorColor = color); StaticText("분",   txtStyle)
    }
}

@Composable private fun StaticText(t: String, style: TextStyle) { Text(t, style = style) }

@Composable
private fun NumSeg(
    value: String,
    onChange: (String) -> Unit,
    width: Dp,
    style: TextStyle,
    maxLen: Int,
    clearOnFocus: Boolean = false,
    padOnBlur: Int? = null,
    cursorColor: Color = MaterialTheme.colorScheme.primary,
) {
    var hadFocus by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = { raw ->
            val digits = raw.filter(Char::isDigit)
            val v = if (digits.length > maxLen) digits.takeLast(maxLen) else digits
            onChange(v)
        },
        textStyle = style,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(cursorColor),
        modifier = Modifier
            .width(width)
            .heightIn(min = 20.dp)
            .onFocusChanged { st ->
                if (st.isFocused) {
                    hadFocus = true
                    if (clearOnFocus) onChange("")
                } else if (hadFocus && padOnBlur != null) {
                    onChange(value.padStart(padOnBlur, '0').take(maxLen))
                }
            },
        decorationBox = { inner -> inner() }
    )
}

/* ───────── helpers ───────── */
private fun toMillisSafe(
    y: String, m: String, d: String, h: String, min: String, zone: ZoneId
): Long? = runCatching {
    val yy  = y.toInt()
    val mm  = m.toInt().coerceIn(1, 12)
    val maxDay = YearMonth.of(yy, mm).lengthOfMonth()
    val dd  = d.toInt().coerceIn(1, maxDay)
    val hh  = h.toInt().coerceIn(0, 23)
    val mi  = min.toInt().coerceIn(0, 59)
    LocalDateTime.of(yy, mm, dd, hh, mi).atZone(zone).toInstant().toEpochMilli()
}.getOrNull()

private fun formatDuration(ms: Long): String {
    val s = ms / 1000
    val d = s / 86400
    val h = (s % 86400) / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return when {
        d > 0 -> "${d}일 ${h}시간 ${m}분 ${sec}초"
        h > 0 -> "${h}시간 ${m}분 ${sec}초"
        m > 0 -> "${m}분 ${sec}초"
        else -> "${max(0, sec)}초"
    }
}
