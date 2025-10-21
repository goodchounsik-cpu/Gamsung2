@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamsung2.domain.plan.Bucket
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.planner.CoursePlan
import com.gamsung2.planner.buildPlans
import com.gamsung2.planner.parseFreeText
import com.gamsung2.ui.story.StoryViewModel

@Composable
fun CourseChatScreen(
    onClose: () -> Unit,
    vm: StoryViewModel = viewModel()
) {
    var input by remember { mutableStateOf("바다와 등산을 함께 갈 수 있는 1시간 거리 코스") }
    var plans by remember { mutableStateOf(emptyList<CoursePlan>()) }
    var isLoading by remember { mutableStateOf(false) }

    val kb = LocalSoftwareKeyboardController.current

    fun run() {
        if (input.isBlank()) return
        isLoading = true
        val q = parseFreeText(input, 37.5665 to 126.9780) // 서울시청 기준 (데모)
        plans = buildPlans(q)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("대화형 코스짜기") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("예) 바다 + 등산, 1시간 거리로") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        capitalization = KeyboardCapitalization.None,
                        keyboardType = KeyboardType.Text,
                        autoCorrect = true
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { kb?.hide(); run() }
                    )
                )
                FilledTonalButton(onClick = { kb?.hide(); run() }) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("추천")
                }
            }
        }
    ) { inner ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (plans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                Text("원하는 코스를 문장으로 말해보세요!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(plans) { p ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("추천 코스", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "① ${p.items[0].title} → ② ${p.items[1].title}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "예상 이동합: 약 ${p.totalDriveMin}분",
                                color = MaterialTheme.colorScheme.outline
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = {
                                        // 두 스탑을 여행스토리(코스)로 담기
                                        p.items.forEach { s ->
                                            vm.addToStory(
                                                PlanItem(
                                                    id = s.id,
                                                    title = s.title,
                                                    lat = s.lat,
                                                    lon = s.lon,
                                                    bucket = Bucket.COURSE
                                                )
                                            )
                                        }
                                        onClose()
                                    }
                                ) { Text("코스로 담기") }
                            }
                        }
                    }
                }
            }
        }
    }
}
