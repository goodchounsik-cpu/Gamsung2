@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamsung2.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit = {}
) {
    // ---- state from VM ----
    val ui by vm.ui.collectAsState()

    // ---- snackbar/coroutine ----
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ---- form state ----
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val email = ui.profile?.email.orEmpty() // 읽기 전용

    // 프로필 로드 및 1회 동기화
    LaunchedEffect(ui.profile) {
        ui.profile?.let {
            name = it.name
            phone = it.phone.orEmpty()
        } ?: vm.loadProfile()
    }

    var editing by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // 변경 여부
    val isDirty by remember(ui.profile, name, phone) {
        derivedStateOf {
            val p = ui.profile
            p != null && (name != p.name || phone != (p.phone ?: ""))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } },
                actions = {
                    if (editing) {
                        TextButton(
                            onClick = {
                                if (isDirty) {
                                    vm.updateProfile(name.trim(), phone.trim().ifBlank { null })
                                    scope.launch { snackbarHostState.showSnackbar("저장했어요") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("변경사항이 없어요") }
                                }
                                editing = false
                            }
                        ) { Text("저장") }
                    } else {
                        TextButton(onClick = { editing = true }) { Text("수정") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (ui.loading && ui.profile == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // ===== 카드: 기본 정보 =====
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "기본 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("이름") },
                        singleLine = true,
                        enabled = editing,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 이메일은 계정 식별자: 읽기 전용(비활성 스타일)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { /* read-only */ },
                        readOnly = true,
                        enabled = false,
                        label = { Text("이메일") },
                        supportingText = { Text("이메일 변경은 별도 절차가 필요해요") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("전화번호(선택)") },
                        singleLine = true,
                        enabled = editing,
                        supportingText = { if (editing) Text("숫자만 입력해도 돼요") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 오류 메시지
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            // ===== 카드: 위험 구역 =====
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "위험 구역",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "회원탈퇴 시 계정이 삭제되고 로그아웃됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("회원탈퇴")
                    }
                }
            }
        }
    }

    // 저장 중 오버레이
    if (ui.profileSaving) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // 탈퇴 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("정말 탈퇴하시겠어요?") },
            text  = { Text("계정이 삭제되고 로그아웃됩니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    vm.withdraw { onDeleted() }
                }) { Text("탈퇴", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }
}
