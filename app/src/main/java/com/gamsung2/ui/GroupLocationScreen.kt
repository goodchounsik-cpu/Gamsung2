// app/src/main/java/com/gamsung2/ui/GroupLocationScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gamsung2.data.local.GroupRepository
import com.gamsung2.services.GroupLocationService
import kotlinx.coroutines.launch

@Composable
fun GroupLocationScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { GroupRepository() }

    var myName by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var myCode by remember { mutableStateOf("") }

    // 그룹 코드가 있으면 멤버 목록 구독
    val membersFlow = remember(myCode) { if (myCode.isNotBlank()) repo.membersFlow(myCode) else null }
    val members: List<Any> by (
            membersFlow?.collectAsState(initial = emptyList<Any>())
                ?: remember { mutableStateOf(emptyList<Any>()) }
            )

    fun startTrackingService(code: String) {
        val intent = Intent(ctx, GroupLocationService::class.java).putExtra("code", code)
        if (Build.VERSION.SDK_INT >= 26) ContextCompat.startForegroundService(ctx, intent)
        else ctx.startService(intent)
    }

    // 표시 문자열(리플렉션으로 name/uid 있으면 사용)
    fun displayOf(any: Any): String {
        val name = runCatching { any.javaClass.getMethod("getName").invoke(any) as String }.getOrNull()
        val uid  = runCatching { any.javaClass.getMethod("getUid").invoke(any) as String }.getOrNull()
        return when {
            name != null && uid != null -> "• $name (${uid.take(6)}…)"
            name != null                -> "• $name"
            any is Map<*, *>            -> "• ${any["name"] ?: any}"
            else                        -> "• $any"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("그룹 위치 공유") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // 내 이름 + 그룹 만들기
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = myName,
                    onValueChange = { myName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("내 이름") },
                    singleLine = true
                )
                Button(onClick = {
                    scope.launch {
                        val code = repo.createGroup()
                        myCode = code
                        codeInput = code
                        startTrackingService(code)
                    }
                }) { Text("그룹 만들기") }
            }

            Spacer(Modifier.height(8.dp))

            // 초대 코드 입력 + 참여
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("초대 코드") },
                    singleLine = true
                )
                Button(onClick = {
                    scope.launch {
                        val code = codeInput.trim()
                        val name = myName.trim()
                        if (code.isNotBlank() && name.isNotBlank()) {
                            repo.joinGroup(code, name)
                            myCode = code
                            startTrackingService(code)
                        }
                    }
                }) { Text("참여") }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            // 멤버 목록
            LazyColumn(
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(if (myCode.isBlank()) "그룹을 만들거나 참여하세요." else "그룹 코드: $myCode")
                }
                items(members) { m ->
                    Text(displayOf(m))
                }
            }
        }
    }

    // 첫 진입 시 익명 로그인 보장
    LaunchedEffect(Unit) {
        repo.ensureAnonSignIn()
    }
}
