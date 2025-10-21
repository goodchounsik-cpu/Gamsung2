// app/src/main/java/com/gamsung2/ui/auth/BizSignUpScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.gamsung2.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamsung2.auth.AuthViewModel

@Composable
fun BizSignUpScreen(
    vm: AuthViewModel = hiltViewModel(),
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    // 폼 상태
    var bizName  by rememberSaveable { mutableStateOf("") }
    var owner    by rememberSaveable { mutableStateOf("") }
    var email    by rememberSaveable { mutableStateOf("") }
    var pw       by rememberSaveable { mutableStateOf("") }
    var address  by rememberSaveable { mutableStateOf("") }
    var mobile   by rememberSaveable { mutableStateOf("") }
    var landline by rememberSaveable { mutableStateOf("") }

    // 성공 다이얼로그
    var showSuccess by rememberSaveable { mutableStateOf(false) }

    fun submit() {
        if (ui.loading) return
        vm.signUpBiz(
            bizName = bizName,
            owner = owner,
            email = email,
            pw = pw,
            address = address,
            mobilePhone = mobile,
            landlinePhone = landline.ifBlank { null },
            onDone = { showSuccess = true } // 가입 성공 → 다이얼로그
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("소상공인 회원가입") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = bizName, onValueChange = { bizName = it },
                label = { Text("업체명") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = owner, onValueChange = { owner = it },
                label = { Text("대표자명") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("이메일") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pw, onValueChange = { pw = it },
                label = { Text("비밀번호 (6자 이상)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("주소") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mobile, onValueChange = { mobile = it },
                label = { Text("휴대전화") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = landline, onValueChange = { landline = it },
                label = { Text("대표전화(선택)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = ::submit,
                enabled = !ui.loading &&
                        bizName.isNotBlank() && owner.isNotBlank() &&
                        email.isNotBlank() && pw.isNotBlank() &&
                        address.isNotBlank() && mobile.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("가입하기")
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { /* 바깥 터치로 닫히지 않게 */ },
            title = { Text("회원가입 완료") },
            text  = { Text("사업자 회원가입이 완료되었습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccess = false
                    onDone() // 확인 → 네비게이션 콜백
                }) { Text("확인") }
            }
        )
    }
}
