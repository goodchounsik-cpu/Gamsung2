// app/src/main/java/com/gamsung2/ui/auth/GeneralSignUpScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.gamsung2.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamsung2.auth.AuthViewModel

@Composable
fun GeneralSignUpScreen(
    onSignedUpAndConfirmed: () -> Unit,   // ✅ 확인 눌렀을 때 네비 이동
    onBack: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    // ✅ 가입 성공 시 띄울 다이얼로그 상태
    var showSuccess by rememberSaveable { mutableStateOf(false) }

    fun doSignUp() {
        if (!ui.loading) {
            vm.signUpGeneral(
                name = name,
                email = email,
                pw = pw,
                phone = phone,
                onDone = { showSuccess = true } // 성공 → 다이얼로그 열기
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("일반 회원가입") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } }
            )
        }
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("이름") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("이메일") }, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pw, onValueChange = { pw = it },
                label = { Text("비밀번호 (6자 이상)") }, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("전화번호") }, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = ::doSignUp,
                enabled = !ui.loading && name.isNotBlank() && email.isNotBlank() && pw.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.loading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("가입하기")
            }
        }
    }

    // ✅ “확인”을 눌러야만 네비 이동
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { /* 바깥 터치로 닫힘 방지 */ },
            title = { Text("회원가입 완료") },
            text  = { Text("회원가입이 완료되었어요. 이제 홈 화면에서 서비스를 이용할 수 있어요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccess = false
                        onSignedUpAndConfirmed()       // 여기서 HOME으로 이동
                    }
                ) { Text("확인") }
            }
        )
    }
}
