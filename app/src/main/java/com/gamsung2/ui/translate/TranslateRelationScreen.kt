@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.translate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gamsung2.ui.translate.TranslateViewModel // ← VM이 여기 패키지면 유지

/** 번역 화면 (언어 드롭다운/음성/읽어주기 포함) */
@Composable
fun TranslateRelationScreen(
    vm: TranslateViewModel,
    onBack: () -> Unit
) {
    val src by vm.srcText.collectAsStateWithLifecycle()
    val dst by vm.dstText.collectAsStateWithLifecycle()
    val srcLang by vm.srcLang.collectAsStateWithLifecycle()
    val dstLang by vm.dstLang.collectAsStateWithLifecycle()
    val downloading by vm.downloading.collectAsStateWithLifecycle()
    val listening by vm.listening.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("번역") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 언어 선택
            LanguagePickers(
                srcCode = srcLang,
                dstCode = dstLang,
                onPickSrc = { code -> vm.setLangs(code, dstLang) },
                onPickDst = { code -> vm.setLangs(srcLang, code) },
                onSwap = vm::swapLangs
            )

            // 입력
            OutlinedTextField(
                value = src,
                onValueChange = vm::updateSrcText,
                label = { Text("원문") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 동작 버튼
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::translate, enabled = !downloading) {
                    Text(if (downloading) "번역 중…" else "번역")
                }
                OutlinedButton(
                    onClick = { vm.startSpeech(srcLang) },
                    enabled = !listening && !downloading
                ) { Text("음성 입력") }
                OutlinedButton(
                    onClick = vm::stopSpeech,
                    enabled = listening
                ) { Text("중지") }
                OutlinedButton(
                    onClick = { vm.speakDst(dstLang) },
                    enabled = dst.isNotBlank()
                ) { Text("읽어주기") }
            }

            // 결과
            OutlinedTextField(
                value = dst,
                onValueChange = {},
                readOnly = true,
                label = { Text(if (downloading) "다운로드/번역 중…" else "번역 결과") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
