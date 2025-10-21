// app/src/main/java/com/gamsung2/ui/auth/SignUpChoiceScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.gamsung2.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SignUpChoiceScreen(
    onGeneral: () -> Unit,
    onBiz: () -> Unit,
    onGov: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("가입 유형 선택") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
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
            ElevatedCard(onClick = onGeneral, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("일반 회원", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("즐겨찾기/코스관리 등 기본 기능")
                }
            }
            ElevatedCard(onClick = onBiz, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("소상공인", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("업체 정보 등록/관리, 홍보 연동")
                }
            }
            ElevatedCard(onClick = onGov, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("관공서/기관", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("공문·행사자료 제출, 승인 현황")
                }
            }
        }
    }
}
