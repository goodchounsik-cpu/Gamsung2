@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.translate

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

@Composable
fun TranslateScreen(
    onBack: (() -> Unit)? = null
) {
    val ctx = LocalContext.current

    var src by remember { mutableStateOf(TranslateLanguage.KOREAN) }
    var tgt by remember { mutableStateOf(TranslateLanguage.ENGLISH) }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var downloading by remember { mutableStateOf(false) }

    // Translator & TTS 생명주기 관리
    var translator by remember { mutableStateOf<Translator?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(src, tgt) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(src)
            .setTargetLanguage(tgt)
            .build()
        val t = Translation.getClient(options)
        translator = t
        onDispose { t.close() }
    }

    DisposableEffect(Unit) {
        val t = TextToSpeech(ctx) { /* init */ }
        tts = t
        onDispose { t.stop(); t.shutdown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("번역") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguagePicker("내 언어", src) { src = it }
            LanguagePicker("상대 언어", tgt) { tgt = it }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                placeholder = { Text("입력(텍스트 또는 마이크)") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = input.isNotBlank() && !downloading,
                    onClick = {
                        val tr = translator ?: return@Button
                        downloading = true
                        tr.downloadModelIfNeeded()
                            .addOnSuccessListener {
                                tr.translate(input)
                                    .addOnSuccessListener { text -> output = text }
                                    .addOnCompleteListener { downloading = false }
                            }
                            .addOnFailureListener { downloading = false }
                    }
                ) { Text(if (downloading) "모델 준비중..." else "번역") }

                OutlinedButton(onClick = { /* TODO: 음성 인식 */ }) {
                    Icon(Icons.Filled.Mic, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("음성 입력")
                }

                OutlinedButton(
                    enabled = output.isNotBlank(),
                    onClick = {
                        tts?.language = Locale.US
                        tts?.speak(output, TextToSpeech.QUEUE_FLUSH, null, "tts-id")
                    }
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("읽어주기")
                }
            }

            OutlinedTextField(
                value = output,
                onValueChange = { output = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                placeholder = { Text("번역 결과") }
            )
        }
    }
}

/** 버전 의존성 없는 간단 드롭다운 */
@Composable
private fun LanguagePicker(
    label: String,
    current: String,
    onChange: (String) -> Unit
) {
    val langs = listOf(
        TranslateLanguage.KOREAN to "한국어",
        TranslateLanguage.ENGLISH to "영어",
        TranslateLanguage.JAPANESE to "일본어",
        TranslateLanguage.CHINESE to "중국어(간체)",
        "zh-Hant" to "중국어(번체)"
    )
    var expanded by remember { mutableStateOf(false) }
    val currentName = langs.firstOrNull { it.first == current }?.second ?: current

    Box {
        OutlinedTextField(
            value = "$label: $currentName",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            langs.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onChange(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
