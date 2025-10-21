@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.gamsung2.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamsung2.auth.AuthViewModel
import com.gamsung2.auth.model.SocialProvider
import com.gamsung2.data.LoginPrefs
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoSignUp: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val focus = LocalFocusManager.current

    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }
    var pwVisible by rememberSaveable { mutableStateOf(false) }
    var rememberId by rememberSaveable { mutableStateOf(false) }
    var navigating by remember { mutableStateOf(false) }

    // ── 아이디 저장값 초기 로드 ──
    LaunchedEffect(Unit) {
        val (remember, lastId) = LoginPrefs.readOnce(ctx)
        rememberId = remember
        if (remember) email = lastId
    }

    fun runAfterLogin() {
        if (!navigating) {
            navigating = true
            onLoggedIn()
        }
        scope.launch {
            snackbarHost.showSnackbar("로그인되었습니다. 환영합니다! 🎉")
        }
    }

    fun doLogin() {
        if (ui.loading || navigating) return
        if (email.isBlank() || pw.isBlank()) return
        focus.clearFocus()
        vm.login(email.trim(), pw) {
            // 로그인 성공 시 아이디 저장 적용
            scope.launch { LoginPrefs.save(ctx, rememberId, email.trim()) }
            runAfterLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("로그인") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize()) {

            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; if (ui.error != null) vm.clearError() },
                    label = { Text("이메일") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pw,
                    onValueChange = { pw = it; if (ui.error != null) vm.clearError() },
                    label = { Text("비밀번호") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { doLogin() }),
                    visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { pwVisible = !pwVisible }) {
                            Icon(
                                imageVector = if (pwVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (pwVisible) "비밀번호 가리기" else "비밀번호 보기"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                // ── 아이디 저장 체크박스 ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberId,
                        onCheckedChange = { rememberId = it }
                    )
                    Text("아이디 저장")
                }

                ui.error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { doLogin() },
                    enabled = !ui.loading && !navigating && email.isNotBlank() && pw.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (ui.loading) CircularProgressIndicator(strokeWidth = 2.dp)
                    else Text("이메일로 로그인")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onGoSignUp,
                    enabled = !ui.loading && !navigating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("회원가입")
                }

                Spacer(Modifier.height(24.dp))
                Text("다른 방법으로 로그인", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!ui.loading && !navigating)
                                vm.loginWithSocial(SocialProvider.KAKAO) {
                                    scope.launch { LoginPrefs.save(ctx, rememberId, email.trim()) }
                                    runAfterLogin()
                                }
                        },
                        enabled = !ui.loading && !navigating,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("카카오로 계속하기") }

                    OutlinedButton(
                        onClick = {
                            if (!ui.loading && !navigating)
                                vm.loginWithSocial(SocialProvider.FACEBOOK) {
                                    scope.launch { LoginPrefs.save(ctx, rememberId, email.trim()) }
                                    runAfterLogin()
                                }
                        },
                        enabled = !ui.loading && !navigating,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("페이스북으로 계속하기") }

                    OutlinedButton(
                        onClick = {
                            if (!ui.loading && !navigating)
                                vm.loginWithSocial(SocialProvider.LINE) {
                                    scope.launch { LoginPrefs.save(ctx, rememberId, email.trim()) }
                                    runAfterLogin()
                                }
                        },
                        enabled = !ui.loading && !navigating,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("라인으로 계속하기") }
                }
            }

            if (ui.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
