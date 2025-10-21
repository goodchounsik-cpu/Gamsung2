package com.gamsung2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment               // ✅ 추가
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun YourInputBar(
    modifier: Modifier = Modifier,
    placeholder: String = "장소 / 작품명 / 카테고리",
    actionLabel: String = "검색",
    imeAction: ImeAction = ImeAction.Search,
    onAction: (String) -> Unit
) {
    var q by remember { mutableStateOf("") }
    val kb = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // 화면 진입 시 자동 포커스 + 키보드 열기
    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        kb?.show()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            singleLine = true,
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.None,
                autoCorrect = true
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (imeAction == ImeAction.Search) {
                        kb?.hide()
                        onAction(q)
                    }
                },
                onSend = {
                    if (imeAction == ImeAction.Send) {
                        kb?.hide()
                        onAction(q)
                    }
                }
            )
        )
        FilledTonalButton(
            onClick = { kb?.hide(); onAction(q) },
            enabled = q.isNotBlank()
        ) {
            Text(actionLabel)
        }
    }
}
