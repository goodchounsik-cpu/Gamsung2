package com.gamsung2.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

// ✅ 여기서도 같은 별칭을 동일하게 사용
import androidx.compose.foundation.text.KeyboardOptions as ComposeKeyboardOptions
import androidx.compose.ui.text.input.KeyboardType as ComposeKeyboardType

@Composable
fun KeyboardOptionsSmokeTest() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        label = { Text("num") },
        keyboardOptions = ComposeKeyboardOptions(keyboardType = ComposeKeyboardType.Number)
    )
}
