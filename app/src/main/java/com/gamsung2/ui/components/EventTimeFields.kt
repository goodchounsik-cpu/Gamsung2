// app/src/main/java/com/gamsung2/ui/components/EventTimeFields.kt
package com.gamsung2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

private val FS_INPUT = 11.sp
private val CHIP_HEIGHT = 32.dp
private val MINI_BOX_WIDTH = 64.dp
private val MINI_RADIUS = 10.dp
private val GAP = 8.dp

// ⬆️ 라벨(“시/분”)을 박스 위로 띄우는 높이
// 경계선에 닿지 않도록 크게 띄움. 필요하면 44.dp까지 더 올려도 OK.
private val LABEL_FLOAT_UP = 42.dp

@Composable
fun ChipBlock(
    isAm: Boolean,
    onAm: () -> Unit,
    onPm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            modifier = Modifier.heightIn(min = CHIP_HEIGHT),
            selected = isAm, onClick = onAm,
            label = { Text("오전", fontSize = FS_INPUT) }
        )
        FilterChip(
            modifier = Modifier.heightIn(min = CHIP_HEIGHT),
            selected = !isAm, onClick = onPm,
            label = { Text("오후", fontSize = FS_INPUT) }
        )
    }
}

@Composable
fun TimeFieldsCompact(
    hour: Int,
    onHour: (Int) -> Unit,
    minute: Int,
    onMinute: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GAP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabeledMiniBox(
            label = "시",
            value = hour.toString().takeIf { it.isNotBlank() } ?: "",
            onValueChange = { it.toIntOrNull()?.coerceIn(1, 12)?.let(onHour) }
        )
        LabeledMiniBox(
            label = "분",
            value = minute.toString().padStart(2, '0'),
            onValueChange = { it.toIntOrNull()?.coerceIn(0, 59)?.let(onMinute) }
        )
    }
}

/** 네모 박스는 그대로 두고 ‘시/분’ 라벨만 위로 크게 띄움 */
@Composable
private fun LabeledMiniBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp = MINI_BOX_WIDTH
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(CHIP_HEIGHT),
        contentAlignment = Alignment.Center
    ) {
        MiniInputBox(
            value = value,
            onValueChange = onValueChange,
            width = width
        )
        // 라벨을 박스 바깥 위로 띄우고, 항상 위에 보이도록 zIndex
        Text(
            text = label,
            fontSize = FS_INPUT,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -LABEL_FLOAT_UP)
                .zIndex(1f)
        )
    }
}

@Composable
private fun MiniInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp
) {
    BasicTextField(
        value = value,
        onValueChange = { s -> onValueChange(s.filter(Char::isDigit).take(2)) },
        singleLine = true,
        textStyle = LocalTextStyle.current.merge(
            TextStyle(fontSize = FS_INPUT, textAlign = TextAlign.Center)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .width(width)
                    .height(CHIP_HEIGHT)
                    .background(Color(0xFFF7F4F9), RoundedCornerShape(MINI_RADIUS))
                    .border(1.dp, Color(0xFFBDB4C7), RoundedCornerShape(MINI_RADIUS)),
                contentAlignment = Alignment.Center
            ) { inner() }
        }
    )
}
