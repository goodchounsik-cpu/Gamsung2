@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.translate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 입력/출력 언어 2개, 중앙 상단 스왑 버튼 */
@Composable
internal fun LanguagePickers(
    srcCode: String,
    dstCode: String,
    onPickSrc: (String) -> Unit,
    onPickDst: (String) -> Unit,
    onSwap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        // 두 박스는 동일 높이 + 동일 폭(weight)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LangDropdown(
                label = "입력 언어",
                selectedCode = srcCode,
                onSelect = onPickSrc,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)     // 동일 높이
            )
            LangDropdown(
                label = "출력 언어",
                selectedCode = dstCode,
                onSelect = onPickDst,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)     // 동일 높이
            )
        }

        // 가운데 위로 살짝 띄운 동그란 스왑 버튼
        FilledTonalIconButton(
            onClick = onSwap,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)  // 살짝 위로
                .size(36.dp)           // 동그랗고 작게
        ) {
            Icon(Icons.Filled.SwapVert, contentDescription = "언어 스왑")
        }
    }
}

/** Material3 ExposedDropdownMenuBox 기반 드롭다운 */
@Composable
internal fun LangDropdown(
    label: String,
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val display = "${codeToLabel(selectedCode)} ($selectedCode)"

    // 살짝 작은 텍스트
    val smallText = TextStyle(fontSize = 13.sp)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {}, // readOnly
            readOnly = true,
            textStyle = smallText,
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(56.dp) // 동일 높이 유지
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupportedLangs.forEach { lang ->
                DropdownMenuItem(
                    text = { Text("${lang.label} (${lang.code})", style = smallText) },
                    onClick = {
                        onSelect(lang.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
