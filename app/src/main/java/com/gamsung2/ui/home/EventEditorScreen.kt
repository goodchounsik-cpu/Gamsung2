// app/src/main/java/com/gamsung2/ui/home/EventEditorScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun EventEditorScreen(
    date: String,
    eventId: Long? = null,   // null=추가, not null=수정
    onClose: () -> Unit
) {
    val isEdit = eventId != null
    var title by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "일정 수정" else "일정 추가") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)          // ⬅ 좌우 공간 확보
                    .fillMaxHeight(0.55f),                // ⬅ 조금 더 키움
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 입력 영역
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        Text("날짜: $date", style = MaterialTheme.typography.labelLarge)

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("제목") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            label = { Text("메모(선택)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // ===== 버튼 바 : [저장, 수정, 취소, 삭제] =====
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val tinyPad = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        val label: @Composable (String) -> Unit = {
                            Text(
                                it,
                                fontSize = 11.sp,                  // ⬅ 더 작게
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp) // ⬅ 간격 축소
                        ) {
                            // 저장 (추가 모드에서 활성)
                            Button(
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("저장 완료 (데모)")
                                        onClose()
                                    }
                                },
                                enabled = !isEdit && title.isNotBlank(),
                                contentPadding = tinyPad,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 36.dp)
                            ) { label("저장") }

                            // 수정 (수정 모드에서 활성)
                            Button(
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("수정 완료 (데모)")
                                        onClose()
                                    }
                                },
                                enabled = isEdit && title.isNotBlank(),
                                contentPadding = tinyPad,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 36.dp)
                            ) { label("수정") }

                            // 취소 (항상 활성)
                            OutlinedButton(
                                onClick = onClose,
                                contentPadding = tinyPad,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 36.dp)
                            ) { label("취소") }

                            // 삭제 (수정 모드에서 활성, 위험색)
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("삭제 완료 (데모)")
                                        onClose()
                                    }
                                },
                                enabled = isEdit,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isEdit)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = tinyPad,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = 0.dp, minHeight = 36.dp)
                            ) { label("삭제") }
                        }
                    }
                }
            }
        }
    }
}
